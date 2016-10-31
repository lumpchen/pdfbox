package org.apache.pdfbox.accessibility.rendering;

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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.StructuredPDFStreamEngine;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

public class PageStructureExtractor extends StructuredPDFStreamEngine {

	private List<MarkedContentNode> contents = new ArrayList<MarkedContentNode>();
	private Map<Integer, MarkedContentNode> markedContentMap = new HashMap<Integer, MarkedContentNode>();
	private Stack<MarkedContentNode> runtimeMarkedContentStack = new Stack<MarkedContentNode>();
	private Stack<COSObject> xobjectStack = new Stack<COSObject>();
	private StringBuilder textBuffer = new StringBuilder();
	
    // clipping winding rule used for the clipping path
    private int clipWindingRule = -1;

    // glyph caches
    private final Map<PDFont, GlyphCache> glyphCaches = new HashMap<PDFont, GlyphCache>();
    
    private GeneralPath linePath = new GeneralPath();

	public PageStructureExtractor(PDPage page) {
		super(page);
	}

	public void extract() throws IOException {
		this.processPage(this.getPage());
		
        for (PDAnnotation annotation : getPage().getAnnotations()) {
            showAnnotation(annotation);
        }
	}
	
    @Override
    public void showAnnotation(PDAnnotation annotation) throws IOException {
    	this.beginAnnot(annotation);
        super.showAnnotation(annotation);
        this.endAnnot(annotation);
    }
    
	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        // to ensure that the path is created in the right direction, we have to create
        // it by combining single lines instead of creating a simple rectangle
        linePath.moveTo((float) p0.getX(), (float) p0.getY());
        linePath.lineTo((float) p1.getX(), (float) p1.getY());
        linePath.lineTo((float) p2.getX(), (float) p2.getY());
        linePath.lineTo((float) p3.getX(), (float) p3.getY());

        // close the subpath instead of adding the last line so that a possible set line
        // cap style isn't taken into account at the "beginning" of the rectangle
        linePath.closePath();
	}

	@Override
	public void drawImage(PDImage pdImage) throws IOException {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();		
        
        AffineTransform imageTransform = new AffineTransform(at);
        Rectangle rect = new Rectangle(0, 0, 1, 1);
        Shape outline = imageTransform.createTransformedShape(rect);
        this.markImage(new GeneralPath(outline));
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

	@Override
	public void shadingFill(COSName shadingName) throws IOException {
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
			this.markPath((GeneralPath) this.getLinePath().clone());			
		}
		this.linePath.reset();
	}
	
	@Override
	public void fillPath(int windingRule) throws IOException {
		this.fillPath((GeneralPath) this.getLinePath().clone(), true, windingRule);
	}
	
	void fillPath(GeneralPath path, boolean mark, int windingRule) throws IOException {
		if (mark) {
			this.markPath((GeneralPath) this.getLinePath().clone());
		}
		this.linePath.reset();
	}
	
	@Override
	public void fillAndStrokePath(int windingRule) throws IOException {
        GeneralPath path = (GeneralPath) this.getLinePath().clone();
        this.fillPath(path, false, windingRule);
        this.strokePath(path, true);
	}

    @Override
    protected void showText(byte[] string) throws IOException {
    	this.textBuffer = new StringBuilder();
    	super.showText(string);
    	
    	this.markText(this.textBuffer.toString());
    	this.textBuffer = null;
    }
    
    @Override
    protected void showFontGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                 Vector displacement) throws IOException {
        AffineTransform at = textRenderingMatrix.createAffineTransform();
        at.concatenate(font.getFontMatrix().createAffineTransform());

        PDVectorFont vectorFont = ((PDVectorFont)font);
        GlyphCache cache = glyphCaches.get(font);
        if (cache == null) {
            cache = new GlyphCache(vectorFont);
            glyphCaches.put(font, cache);
        }
        
        GeneralPath glyph2D = cache.getPathForCharacterCode(code);
        this.textBuffer.append(unicode);
        drawGlyph2D(glyph2D, font, code, displacement, at);
    }
    
    protected void drawGlyph2D(GeneralPath glyph2D, PDFont font, int code, Vector displacement,
            AffineTransform at) throws IOException {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();
        
        if (glyph2D != null) {
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
            Shape glyph = at.createTransformedShape(glyph2D);
            this.markPath(glyph.getBounds());
            
            if (renderingMode.isFill()) {
            }

            if (renderingMode.isStroke()) {
            }

            if (renderingMode.isClip()) {
            }
    	}
    }
    
	private void markImage(Shape outline) {
		if (this.runtimeMarkedContentStack.isEmpty()) {
    		return;
    	}
		MarkedContentNode content = this.runtimeMarkedContentStack.peek();
		content.addOutlineShape(outline);
	}
	
	private void markPath(Shape gpath) {
    	if (this.runtimeMarkedContentStack.isEmpty()) {
    		return;
    	}
    	MarkedContentNode content = this.runtimeMarkedContentStack.peek();
    	content.addOutlineShape(gpath);
	}
	
    private void markText(String text) {
    	if (this.runtimeMarkedContentStack.isEmpty()) {
    		return;
    	}
    	MarkedContentNode content = this.runtimeMarkedContentStack.peek();
    	content.appendContentString(text);
    }
    
    @Override
	public void beginMarkedContentSequence(COSName tag, COSDictionary properties) {
		PDMarkedContent markedContent = PDMarkedContent.create(tag, properties);
		
		if (!this.runtimeMarkedContentStack.isEmpty()) {
			MarkedContentNode parentNode = this.runtimeMarkedContentStack.peek();
			if (parentNode != null) {
				MarkedContentNode newChild = new MarkedContentNode(markedContent);
				parentNode.addMarkedContentNode(newChild);
				this.runtimeMarkedContentStack.push(newChild);
				return;
			}
		}

		MarkedContentNode newNode = new MarkedContentNode(markedContent);
		this.contents.add(newNode);
		
		if (!newNode.isArtifact()) {
			int mcid = markedContent.getMCID();
			if (mcid >= 0) {
				this.markedContentMap.put(mcid, newNode);
			}
		}
		
		this.runtimeMarkedContentStack.push(newNode);
	}
	
	public void endMarkedContentSequence() {
		if (this.runtimeMarkedContentStack.isEmpty()) {
			return;
		}
		MarkedContentNode last = this.runtimeMarkedContentStack.pop();
		if (!this.xobjectStack.isEmpty()) {
			last.setXObjectRefTag(this.xobjectStack.peek());
		}
	}
	
	public List<MarkedContentNode> getMarkedContentNodeList() {
		return this.contents;
	}

	public void beginXObject(COSObject xobject) {
		this.xobjectStack.push(xobject);
	}
	
	public void endXObject() {
		if (this.xobjectStack.isEmpty()) {
			return;
		}
		this.xobjectStack.pop();
	}

	public void beginAnnot(PDAnnotation annot) {
		int structParent = annot.getStructParent();
	}

	public void endAnnot(PDAnnotation annot) {
		int structParent = annot.getStructParent();
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
