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

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType0;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.CIDType0Glyph2D;
import org.apache.pdfbox.rendering.Glyph2D;
import org.apache.pdfbox.rendering.TTFGlyph2D;
import org.apache.pdfbox.rendering.Type1Glyph2D;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

public class PageContentExtractor extends PDFGraphicsStreamEngine {

	private List<PageContent> contentList;
	private GeneralPath linePath = new GeneralPath();
	private int clipWindingRule = -1;

	private Stack<PageContent> runtimePageContentStack = new Stack<PageContent>();
	
	private final Map<PDFont, Glyph2D> fontGlyph2D = new HashMap<PDFont, Glyph2D>();

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

	@Override
	public void showAnnotation(PDAnnotation annotation) throws IOException {
		this.beginAnnot(annotation);
		super.showAnnotation(annotation);
		this.endAnnot(annotation);
	}

	public void beginAnnot(PDAnnotation annot) {
	}

	public void endAnnot(PDAnnotation annot) {
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

	protected final GeneralPath getLinePath() {
		return linePath;
	}

	@Override
	public void strokePath() throws IOException {
		this.strokePath((GeneralPath) this.getLinePath().clone(), true);
	}

	void strokePath(GeneralPath path, boolean mark) throws IOException {
		if (mark) {
			this.markPathContent();
		}
		this.linePath.reset();
	}

	@Override
	public void fillPath(int windingRule) throws IOException {
		this.fillPath((GeneralPath) this.getLinePath().clone(), true, windingRule);
	}

	void fillPath(GeneralPath path, boolean mark, int windingRule) throws IOException {
		if (mark) {
			this.markPathContent();
		}
		this.linePath.reset();
	}
	
	private void markPathContent() {
		PathContent content = new PathContent();
		this.runtimePageContentStack.push(content);
		this.markGraphicsState();
		this.markPath((GeneralPath) this.getLinePath().clone());
		this.contentList.add(this.runtimePageContentStack.pop());
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
    	this.contentList.add(content);
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

		Glyph2D glyph2D = createGlyph2D(font);
		this.markText(unicode, code);
		drawGlyph2D(glyph2D, font, code, displacement, at);
	}

	protected void drawGlyph2D(Glyph2D glyph2D, PDFont font, int code, Vector displacement, AffineTransform at)
			throws IOException {
		GeneralPath path = glyph2D.getPathForCharacterCode(code);
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
			this.markPath(glyph.getBounds());
		}
	}

	private Glyph2D createGlyph2D(PDFont font) throws IOException {
		if (fontGlyph2D.containsKey(font)) {
			return fontGlyph2D.get(font);
		}

		Glyph2D glyph2D = null;
		if (font instanceof PDTrueTypeFont) {
			PDTrueTypeFont ttfFont = (PDTrueTypeFont) font;
			glyph2D = new TTFGlyph2D(ttfFont); // TTF is never null
		} else if (font instanceof PDType1Font) {
			PDType1Font pdType1Font = (PDType1Font) font;
			glyph2D = new Type1Glyph2D(pdType1Font); // T1 is never null
		} else if (font instanceof PDType1CFont) {
			PDType1CFont type1CFont = (PDType1CFont) font;
			glyph2D = new Type1Glyph2D(type1CFont);
		} else if (font instanceof PDType0Font) {
			PDType0Font type0Font = (PDType0Font) font;
			if (type0Font.getDescendantFont() instanceof PDCIDFontType2) {
				glyph2D = new TTFGlyph2D(type0Font); // TTF is never null
			} else if (type0Font.getDescendantFont() instanceof PDCIDFontType0) {
				// a Type0 CIDFont contains CFF font
				PDCIDFontType0 cidType0Font = (PDCIDFontType0) type0Font.getDescendantFont();
				glyph2D = new CIDType0Glyph2D(cidType0Font);
			}
		} else {
			throw new IllegalStateException("Bad font type: " + font.getClass().getSimpleName());
		}

		// cache the Glyph2D instance
		if (glyph2D != null) {
			fontGlyph2D.put(font, glyph2D);
		}

		if (glyph2D == null) {
			throw new UnsupportedOperationException("No font for " + font.getName());
		}

		return glyph2D;
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

	private void markImage(Shape outline) {
		if (this.runtimePageContentStack.isEmpty()) {
			return;
		}
		PageContent content = this.runtimePageContentStack.peek();
		content.addOutlineShape(outline);
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
	
	private static void toColorDesc(PDColor pdColor, ColorDesc colorDesc) {
    	if (pdColor.isPattern()) {
    		colorDesc.patternName = pdColor.getPatternName().getName();
    	}
    	colorDesc.components = pdColor.getComponents();
    	colorDesc.colorSpace = pdColor.getColorSpace().getName();
	}
}
