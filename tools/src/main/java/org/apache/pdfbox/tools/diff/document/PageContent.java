package org.apache.pdfbox.tools.diff.document;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

public abstract class PageContent {

	public static enum Type {
		Text, Path, Annot, xObject, Image
	};

	protected Type type;
	protected List<Shape> outline;
	private Area area;
	
	private GraphicsStateDesc gstate;

	public static class GraphicsStateDesc {
		public TextStateDesc textState;
		
		public ColorDesc nonStrokingColor;
		public ColorDesc strokingColor;
		
		public float lineWidth;
		public int lineCap;
		public int lineJoin;
		public float miterLimit;
		
//        private Matrix currentTransformationMatrix = new Matrix();
//        private PDColor strokingColor = PDDeviceGray.INSTANCE.getInitialColor();
//        private PDColor nonStrokingColor = PDDeviceGray.INSTANCE.getInitialColor();
//        private PDColorSpace strokingColorSpace = PDDeviceGray.INSTANCE;
//        private PDColorSpace nonStrokingColorSpace = PDDeviceGray.INSTANCE;
//        private PDTextState textState = new PDTextState();
//        private float lineWidth = 1;
//        private int lineCap = BasicStroke.CAP_BUTT;
//        private int lineJoin = BasicStroke.JOIN_MITER;
//        private float miterLimit = 10;
//        private PDLineDashPattern lineDashPattern = new PDLineDashPattern();
//        private RenderingIntent renderingIntent;
//        private boolean strokeAdjustment = false;
//        private BlendMode blendMode = BlendMode.COMPATIBLE;
//        private PDSoftMask softMask;
//        private double alphaConstant = 1.0;
//        private double nonStrokingAlphaConstant = 1.0;
//        private boolean alphaSource = false;
//        private boolean overprint = false;
//        private double overprintMode = 0;
//        private COSBase transfer = null;
//        private double flatness = 1.0;
//        private double smoothness = 0;
	}
	
	public static class TextStateDesc {
		public float characterSpacing = 0;
		public float wordSpacing = 0;
		public float horizontalScaling = 100;
		public float leading = 0;
        public float fontSize;
        public RenderingMode renderingMode;
        public float rise = 0;
        public boolean knockout = true;
        public String fontName;
	}
	
	public static class ColorDesc {
	    public float[] components;
	    public String patternName;
	    public String colorSpace;
	}
	
	public PageContent() {
	}

	public void addOutlineShape(Shape shape) {
		if (this.outline == null) {
			this.outline = new ArrayList<Shape>();
		}
		this.outline.add(shape);
	}
	
    public Area getOutlineArea() {
    	if (this.area != null) {
    		return this.area;
    	}
    	this.area = new Area();
    	if (this.outline != null) {
    		for (Shape s : this.outline) {
    			if (s instanceof GeneralPath) {
    				this.area.add(new Area(((GeneralPath) s).getBounds()));
    			} else {
    				this.area.add(new Area(s));    				
    			}
        	}
    	}
    	return this.area;
    }

	public Type getType() {
		return this.type;
	}
	
	public void setGraphicsStateDesc(GraphicsStateDesc gstate) {
		this.gstate = gstate;
	}
	
	public GraphicsStateDesc getGraphicsStateDesc() {
		return this.gstate;
	}
	
	public String getNonStrokingColorspace() {
		if (this.getGraphicsStateDesc() == null
				|| this.getGraphicsStateDesc().nonStrokingColor == null) {
			return null;
		}
		return this.getGraphicsStateDesc().nonStrokingColor.colorSpace;
	}
	
	public String getFontName() {
		if (this.getGraphicsStateDesc() == null 
				|| this.getGraphicsStateDesc().textState == null) {
			return null;
		}
		return this.getGraphicsStateDesc().textState.fontName;
	}
	
	public Float getFontSize() {
		if (this.getGraphicsStateDesc() == null 
				|| this.getGraphicsStateDesc().textState == null) {
			return null;
		}
		return this.getGraphicsStateDesc().textState.fontSize;
	}
	

	abstract public String showString();
	abstract public String getTypeString();
	
	abstract public Map<String, String> getAttrMap();
	abstract public boolean diff(PageContent content);
	
	public static class TextContent extends PageContent {

		private StringBuilder text;
		private List<Integer> cidArray;
		
		public TextContent() {
			super();
			this.type = Type.Text;
			this.text = new StringBuilder();
			this.cidArray = new ArrayList<Integer>();
		}	
		
		public void appendText(String unicode, Integer cid) {
			this.text.append(unicode);
			this.cidArray.add(cid);
		}

		public String getText() {
			return this.text.toString();
		}
		
		@Override
		public String showString() {
			return this.text.toString();
		}
		
		public int cidAt(int index) {
			if (index < 0 || index >= this.cidArray.size()) {
				throw new IllegalArgumentException("Out of CID array range.");
			}
			return this.cidArray.get(index);
		}

		@Override
		public String getTypeString() {
			return "Text";
		}
		
		@Override
		public Map<String, String> getAttrMap() {
			return null;
		}

		@Override
		public boolean diff(PageContent content) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public static class PathContent extends PageContent {

		public PathContent() {
			super();
			this.type = Type.Path;
		}
		
		@Override
		public String showString() {
			return "";
		}

		@Override
		public String getTypeString() {
			return "Path";
		}
		
		@Override
		public Map<String, String> getAttrMap() {
			return null;
		}

		@Override
		public boolean diff(PageContent content) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public static class ImageContent extends PageContent {
		
		public int bitsPerComponent;
		public int byteCount;
		public String colorSpace;
		public String decode;
		public int height;
		public int width;
		public String suffix;
		
		public ImageContent() {
			super();
			this.type = Type.Image;
		}
		
		@Override
		public String showString() {
			return "";
		}

		@Override
		public String getTypeString() {
			return "Image";
		}
		
		@Override
		public Map<String, String> getAttrMap() {
			return null;
		}

		@Override
		public boolean diff(PageContent content) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public static class AnnotContent extends PageContent {
		
		public String subType;
		public String fieldType;
		public String annotName;
		public String annotContents;
		
		public AnnotContent() {
			super();
			this.type = Type.Annot;
		}
		
		@Override
		public String showString() {
			return "";
		}

		@Override
		public String getTypeString() {
			return "Annot";
		}
		
		@Override
		public Map<String, String> getAttrMap() {
			return null;
		}

		@Override
		public boolean diff(PageContent content) {
			// TODO Auto-generated method stub
			return false;
		}
	}
}
