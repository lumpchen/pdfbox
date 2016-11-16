package org.apache.pdfbox.tools.diff.document;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.tools.diff.document.PageContent.AnnotContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageContent.Type;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

public class PageContentExtractor extends PDFGraphicsStreamEngine {

	private List<PageContent> contentList;
	private GeneralPath linePath = new GeneralPath();
	private int clipWindingRule = -1;

	private Stack<PageContent> runtimePageContentStack = new Stack<PageContent>();
	
    // glyph caches
    private final Map<PDFont, GlyphCache> glyphCaches = new HashMap<PDFont, GlyphCache>();

	public PageContentExtractor(PDPage page) {
		super(page);

		this.contentList = new ArrayList<PageContent>();
	}

	public void extract() throws IOException {
		this.processPage(this.getPage());

		for (PDAnnotation annotation : getPage().getAnnotations()) {
			showAnnotation(annotation);
		}
	}

	public List<PageContent> getPageContentList() {
		return this.contentList;
	}

	private void addToContentList(PageContent content) {
		if (!this.runtimePageContentStack.isEmpty()) {
			PageContent last = this.runtimePageContentStack.peek();
			if (last.getType() == Type.Annot) {
				((AnnotContent) last).addAppearanceContent(content);
				return;
			}
		}
		
		this.contentList.add(content);
	}
	
	@Override
	public void showAnnotation(PDAnnotation annotation) throws IOException {
		this.beginAnnot(annotation);
		super.showAnnotation(annotation);
		this.endAnnot(annotation);
	}

	public void beginAnnot(PDAnnotation annot) {
		AnnotContent content = new AnnotContent();
		this.runtimePageContentStack.push(content);
		this.markAnnot(annot, content);
	}

	public void endAnnot(PDAnnotation annot) {
		if (this.runtimePageContentStack.isEmpty()) {
			return;
		}
		
		PageContent content = this.runtimePageContentStack.pop();
		this.contentList.add(content);
	}
	
	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
		this.linePath.moveTo((float) p0.getX(), (float) p0.getY());
		this.linePath.lineTo((float) p1.getX(), (float) p1.getY());
		this.linePath.lineTo((float) p2.getX(), (float) p2.getY());
		this.linePath.lineTo((float) p3.getX(), (float) p3.getY());

		this.linePath.closePath();
	}

	@Override
	public void drawImage(PDImage pdImage) throws IOException {
		Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
		AffineTransform at = ctm.createAffineTransform();

		AffineTransform imageTransform = new AffineTransform(at);
		Rectangle rect = new Rectangle(0, 0, 1, 1);
		Shape outline = imageTransform.createTransformedShape(rect);
		this.markImage(pdImage, new GeneralPath(outline));
	}

	@Override
	public void clip(int windingRule) throws IOException {
		this.clipWindingRule = windingRule;
	}

	@Override
	public void moveTo(float x, float y) throws IOException {
		this.linePath.moveTo(x, y);
	}

	@Override
	public void lineTo(float x, float y) throws IOException {
		this.linePath.lineTo(x, y);
	}

	@Override
	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
		this.linePath.curveTo(x1, y1, x2, y2, x3, y3);
	}

	@Override
	public Point2D getCurrentPoint() throws IOException {
		return this.linePath.getCurrentPoint();
	}

	@Override
	public void closePath() throws IOException {
		this.linePath.closePath();
	}

	@Override
	public void endPath() throws IOException {
		if (this.clipWindingRule != -1) {
			this.linePath.setWindingRule(this.clipWindingRule);
			getGraphicsState().intersectClippingPath(this.linePath);
			this.clipWindingRule = -1;
		}
		this.linePath.reset();
	}

	protected final GeneralPath getLinePath() {
		return linePath;
	}

	@Override
	public void strokePath() throws IOException {
		this.strokePath((GeneralPath) this.getLinePath().clone(), true);
	}

	void strokePath(GeneralPath path, boolean mark) throws IOException {
		if (mark) {
			this.markPathContent(false);
		}
		this.linePath.reset();
	}

	@Override
	public void fillPath(int windingRule) throws IOException {
		this.fillPath((GeneralPath) this.getLinePath().clone(), true, windingRule);
	}

	private void fillPath(GeneralPath path, boolean mark, int windingRule) throws IOException {
		if (mark) {
			this.markPathContent(true);
		}
		this.linePath.reset();
	}
	
	private void markPathContent(boolean fill) {
		PathContent content = new PathContent(fill);
		this.runtimePageContentStack.push(content);
		this.markGraphicsState();
		
		GeneralPath gpath = (GeneralPath) this.getLinePath().clone();
		this.markPath(gpath);
		this.addToContentList(this.runtimePageContentStack.pop());
	}

	@Override
	public void fillAndStrokePath(int windingRule) throws IOException {
		GeneralPath path = (GeneralPath) this.getLinePath().clone();
		this.fillPath(path, false, windingRule);
		this.strokePath(path, true);
	}

	@Override
	public void shadingFill(COSName shadingName) throws IOException {
	}

    public void beginText() throws IOException {
    	TextContent content = new TextContent();
    	this.runtimePageContentStack.push(content);
    }

    public void endText() throws IOException {
    	if (this.runtimePageContentStack.isEmpty()) {
    		return;
    	}
    	PageContent content = this.runtimePageContentStack.pop();
    	
    	if (content.getType() == PageContent.Type.Text) {
    		TextContent textContent = (TextContent) content;
    		if (textContent.getText() == null 
    				|| textContent.getText().trim().length() == 0
    				|| textContent.getHeight() == 0) { //empty TJ
    			return;
    		}
    	}
    	this.addToContentList(content);
    }
    
	@Override
	protected void showText(byte[] string) throws IOException {
		this.markGraphicsState();
		super.showText(string);
	}

	@Override
	protected void showFontGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode, Vector displacement)
			throws IOException {
		AffineTransform at = textRenderingMatrix.createAffineTransform();
		at.concatenate(font.getFontMatrix().createAffineTransform());

        PDVectorFont vectorFont = ((PDVectorFont) font);
        GlyphCache cache = glyphCaches.get(font);
        if (cache == null) {
            cache = new GlyphCache(vectorFont);
            glyphCaches.put(font, cache);
        }
        
        GeneralPath path = cache.getPathForCharacterCode(code);
        drawGlyph(path, font, code, displacement, at);
        
		this.markText(unicode, code);
	}

    private void drawGlyph(GeneralPath path, PDFont font, int code, Vector displacement, 
    		AffineTransform at) throws IOException {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();
        
        if (path != null) {
            // stretch non-embedded glyph if it does not match the width contained in the PDF
            if (!font.isEmbedded()) {
                float fontWidth = font.getWidthFromFont(code);
                if (fontWidth > 0 && // ignore spaces
                        Math.abs(fontWidth - displacement.getX() * 1000) > 0.0001) {
                    float pdfWidth = displacement.getX() * 1000;
                    at.scale(pdfWidth / fontWidth, 1);
                }
            }

            // render glyph
            Shape glyph = at.createTransformedShape(path);
            this.markPath(glyph.getBounds2D());
            
            if (renderingMode.isFill()) {
            }

            if (renderingMode.isStroke()) {
            }

            if (renderingMode.isClip()) {
            }
        }
    }

	private void markText(String unicode, Integer cid) {
		if (this.runtimePageContentStack.isEmpty()) {
			return;
		}
		PageContent content = this.runtimePageContentStack.peek();
		if (content.getType() != PageContent.Type.Text) {
			return;
		}
		TextContent textContent = (TextContent) content;
		textContent.appendText(unicode, cid);
	}

	private void markImage(PDImage pdImage, Shape outline) {
		ImageContent content = new ImageContent();
		this.runtimePageContentStack.push(content);
		
		content.bitsPerComponent = pdImage.getBitsPerComponent();
		try {
			if (pdImage.getColorSpace() != null) {
				content.colorSpace = pdImage.getColorSpace().getName();
			}
		} catch (IOException e) {
		}
		if (pdImage.getDecode() != null) {
			content.decode = pdImage.getDecode().toString();
		}
		content.height = pdImage.getHeight();
		content.width = pdImage.getWidth();
		content.suffix = pdImage.getSuffix();
		try {
			content.byteCount = pdImage.createInputStream().available();
		} catch (IOException e) {
		}
		
		this.markPath(outline);
		this.addToContentList(this.runtimePageContentStack.pop());
		
	}

	private void markPath(Shape gpath) {
		if (this.runtimePageContentStack.isEmpty()) {
			return;
		}
		PageContent content = this.runtimePageContentStack.peek();
		content.addOutlineShape(gpath);
	}
	
	private void markGraphicsState() {
		if (this.runtimePageContentStack.isEmpty()) {
			return;
		}
		PageContent content = this.runtimePageContentStack.peek();
		
        PDGraphicsState state = getGraphicsState();
        PDTextState textState = state.getTextState();
        
        PageContent.GraphicsStateDesc gstate = new PageContent.GraphicsStateDesc();
		
		gstate.textState = new PageContent.TextStateDesc();
		gstate.textState.characterSpacing = textState.getCharacterSpacing();
		gstate.textState.wordSpacing = textState.getWordSpacing();
		gstate.textState.horizontalScaling = textState.getHorizontalScaling();
		gstate.textState.leading = textState.getLeading();
		gstate.textState.fontSize = textState.getFontSize();
		gstate.textState.renderingMode = textState.getRenderingMode();
		gstate.textState.rise = textState.getRise();
		gstate.textState.knockout = textState.getKnockoutFlag();
        PDFont font = textState.getFont();
        if (font != null) {
        	gstate.textState.fontName = font.getName();
        }
        
        gstate.lineWidth = state.getLineWidth();
        gstate.lineCap = state.getLineCap();
        gstate.lineJoin = state.getLineJoin();
        gstate.miterLimit = state.getMiterLimit();

        if (state.getNonStrokingColor() != null) {
        	gstate.nonStrokingColor = new ColorDesc();
        	toColorDesc(state.getNonStrokingColor(), gstate.nonStrokingColor);
        }
        
        if (state.getStrokingColor() != null) {
        	gstate.strokingColor = new ColorDesc();
        	toColorDesc(state.getStrokingColor(), gstate.strokingColor);
        }
        
        content.setGraphicsStateDesc(gstate);
	}
	
	private void markAnnot(PDAnnotation annot, AnnotContent content) {
		content.subType = annot.getSubtype();
		
		COSDictionary parent = null;
		if (annot.getCOSObject().getDictionaryObject(COSName.PARENT) != null) {
			parent = (COSDictionary) annot.getCOSObject().getDictionaryObject(COSName.PARENT);			
		}
		
		if (annot.getCOSObject().getCOSName(COSName.FT) == null) {
			if (parent != null) {
				content.fieldType = parent.getCOSName(COSName.FT).getName();	
			}
		} else {
			content.fieldType = annot.getCOSObject().getCOSName(COSName.FT).getName();
		}
		
		if (annot.getRectangle() != null) {
			GeneralPath rect = annot.getRectangle().toGeneralPath();
			content.addOutlineShape(rect);
		}
		
		content.annotName = annot.getCOSObject().getString(COSName.T);
		if (content.annotName == null) {
			if (parent != null) {
				content.annotName = parent.getString(COSName.T);	
			}
		}
		
		content.annotContents = annot.getCOSObject().getString(COSName.TU);
		if (content.annotContents == null) {
			if (parent != null) {
				content.annotContents = parent.getString(COSName.TU);	
			}
		}
	}
	
	private static void toColorDesc(PDColor pdColor, ColorDesc colorDesc) {
    	if (pdColor.isPattern()) {
    		colorDesc.patternName = pdColor.getPatternName().getName();
    	}
    	colorDesc.components = pdColor.getComponents();
    	colorDesc.colorSpace = pdColor.getColorSpace().getName();
	}
	
	
	static class GlyphCache {
	    private static final Log LOG = LogFactory.getLog(GlyphCache.class);
	    
	    private final PDVectorFont font;
	    private final Map<Integer, GeneralPath> cache = new HashMap<Integer, GeneralPath>();

	    public GlyphCache(PDVectorFont font) {
	        this.font = font;
	    }
	    
	    public GeneralPath getPathForCharacterCode(int code) {
	        GeneralPath path = cache.get(code);
	        if (path != null) {
	            return path;
	        }

	        try {
	            if (!font.hasGlyph(code)) {
	                String fontName = ((PDFont)font).getName();
	                if (font instanceof PDType0Font) {
	                    int cid = ((PDType0Font) font).codeToCID(code);
	                    String cidHex = String.format("%04x", cid);
	                    LOG.warn("No glyph for " + code + " (CID " + cidHex + ") in font " + fontName);
	                }
	                else {
	                    LOG.warn("No glyph for " + code + " in font " + fontName);
	                }
	            }

	            path = font.getNormalizedPath(code);
	            cache.put(code, path);
	            return path;
	        }
	        catch (IOException e) {
	            // todo: escalate this error?
	            LOG.error("Glyph rendering failed", e);
	            return new GeneralPath();
	        }
	    }
	}
}
