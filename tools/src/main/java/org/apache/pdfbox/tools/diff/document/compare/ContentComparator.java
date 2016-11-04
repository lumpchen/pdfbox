package org.apache.pdfbox.tools.diff.document.compare;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.GraphicsStateDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.TextStateDesc;
import org.apache.pdfbox.tools.diff.document.PageThread;

public abstract class ContentComparator {

	CompareSetting setting;

	public ContentComparator(CompareSetting setting) {
		this.setting = setting;
	}

	public abstract DiffContent[] compare(PageThread basePageThread, PageThread testPageThread);

	protected boolean compare(Float f1, Float f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return Math.abs(f1 - f2) <= 0.00001;
	}

	protected boolean compare(Integer f1, Integer f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return f1.intValue() == f2.intValue();
	}

	protected boolean compare(GraphicsStateDesc gstate_1, GraphicsStateDesc gstate_2) {
		if (gstate_1 == null && gstate_2 == null) {
			return true;
		}
		if (gstate_1 != null && gstate_2 != null) {
			boolean b = compare(gstate_1.textState, gstate_2.textState);
			b &= compare(gstate_1.nonStrokingColor, gstate_2.nonStrokingColor);
			b &= compare(gstate_1.strokingColor, gstate_2.strokingColor);
			return b;
		}
		return false;
	}

	protected boolean compare(TextStateDesc tstate_1, TextStateDesc tstate_2) {
		if (tstate_1 == null && tstate_2 == null) {
			return true;
		}
		if (tstate_1 != null && tstate_2 != null) {
			boolean b = tstate_1.characterSpacing == tstate_2.characterSpacing
					&& tstate_1.wordSpacing == tstate_2.wordSpacing
					&& tstate_1.horizontalScaling == tstate_2.horizontalScaling && tstate_1.leading == tstate_2.leading
					&& tstate_1.fontSize == tstate_2.fontSize && tstate_1.renderingMode == tstate_2.renderingMode
					&& tstate_1.rise == tstate_2.rise && tstate_1.knockout == tstate_2.knockout;
			b &= compare(tstate_1.fontName, tstate_2.fontName);
			return b;
		}
		return false;
	}

	protected boolean compare(ColorDesc color_1, ColorDesc color_2) {
		if (color_1 == null && color_2 == null) {
			return true;
		}
		if (color_1 != null && color_2 != null) {
			boolean b = compare(color_1.patternName, color_2.patternName);
			b &= compare(color_1.colorSpace, color_2.colorSpace);
			b &= compare(color_1.components, color_2.components);
			return b;
		}
		return false;
	}

	protected boolean compare(float[] arr1, float[] arr2) {
		if (arr1 == null && arr2 == null) {
			return true;
		}
		if (arr1 != null && arr2 != null) {
			if (arr1.length != arr2.length) {
				return false;
			}
			for (int i = 0; i < arr1.length; i++) {
				if (arr1[i] != arr2[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	protected boolean compare(String s1, String s2) {
		if (s1 != null) {
			return s1.equals(s2);
		}
		if (s2 != null) {
			return s2.equals(s1);
		}
		return true;
	}

	protected String asString(Rectangle2D rect) {
		if (rect == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		buf.append("x=" + rect.getX());
		buf.append(", ");
		buf.append("y=" + rect.getY());
		buf.append(", ");
		buf.append("width=" + rect.getWidth());
		buf.append(", ");
		buf.append("height=" + rect.getHeight());
		return buf.toString();
	}

	protected String removeFontNameSuffix(String fontName) {
		if (fontName == null) {
			return null;
		}
		if (fontName.indexOf("+") > 0) {
			return fontName.substring(fontName.indexOf("+"), fontName.length() - 1);
		}
		return fontName;
	}

	protected BufferedImage diffImages(BufferedImage bim1, BufferedImage bim2) throws IOException {
		int minWidth = Math.min(bim1.getWidth(), bim2.getWidth());
		int minHeight = Math.min(bim1.getHeight(), bim2.getHeight());
		int maxWidth = Math.max(bim1.getWidth(), bim2.getWidth());
		int maxHeight = Math.max(bim1.getHeight(), bim2.getHeight());
		BufferedImage bim3 = null;
		if (minWidth != maxWidth || minHeight != maxHeight) {
			bim3 = createEmptyDiffImage(minWidth, minHeight, maxWidth, maxHeight);
		}
		for (int x = 0; x < minWidth; ++x) {
			for (int y = 0; y < minHeight; ++y) {
				int rgb1 = bim1.getRGB(x, y);
				int rgb2 = bim2.getRGB(x, y);
				if (rgb1 != rgb2
						// don't bother about differences of 1 color step
						&& (Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) > 1
								|| Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) > 1
								|| Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) > 1)) {
					if (bim3 == null) {
						bim3 = createEmptyDiffImage(minWidth, minHeight, maxWidth, maxHeight);
					}
					int r = Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));
					int g = Math.abs((rgb1 & 0xFF00) - (rgb2 & 0xFF00));
					int b = Math.abs((rgb1 & 0xFF0000) - (rgb2 & 0xFF0000));
					bim3.setRGB(x, y, 0xFFFFFF - (r | g | b));
				} else {
					if (bim3 != null) {
						bim3.setRGB(x, y, Color.WHITE.getRGB());
					}
				}
			}
		}
		return bim3;
	}

	protected BufferedImage createEmptyDiffImage(int minWidth, int minHeight, int maxWidth, int maxHeight) {
		BufferedImage bim3 = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = bim3.getGraphics();
		if (minWidth != maxWidth || minHeight != maxHeight) {
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, maxWidth, maxHeight);
		}
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, minWidth, minHeight);
		graphics.dispose();
		return bim3;
	}
}
