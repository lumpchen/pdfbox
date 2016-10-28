package org.apache.pdfbox.tools.diff.document;

import java.awt.Rectangle;
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
		
		@Override
	    public boolean equals(Object obj) {
	        if (this == obj) {
	        	return true;
	        }
	        if (obj == null) {
	        	return false;
	        }
	        
	        GraphicsStateDesc aObj = (GraphicsStateDesc) obj;
	        if (this.textState != null && this.textState.equals(aObj.textState)
	        		&& this.nonStrokingColor != null && this.nonStrokingColor.equals(aObj.nonStrokingColor)
	        		&& this.strokingColor != null && this.strokingColor.equals(aObj.strokingColor)
	        		&& this.lineWidth == aObj.lineWidth
	        		&& this.lineCap == aObj.lineCap
	        		&& this.lineJoin == aObj.lineJoin
	        		&& this.miterLimit == aObj.miterLimit) {
	        	return true;
	        }
	        return false;
	    }
		
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
        
		@Override
	    public boolean equals(Object obj) {
	        if (this == obj) {
	        	return true;
	        }
	        if (obj == null) {
	        	return false;
	        }
	        
	        TextStateDesc aObj = (TextStateDesc) obj;
	        if (this.characterSpacing == aObj.characterSpacing
	        		&& this.wordSpacing == aObj.wordSpacing
	        		&& this.horizontalScaling == aObj.horizontalScaling
	        		&& this.leading == aObj.leading
	        		&& this.fontSize == aObj.fontSize
	        		&& this.renderingMode == aObj.renderingMode
	        		&& this.rise == aObj.rise
	        		&& this.knockout == aObj.knockout
	        		&& this.fontName == aObj.fontName) {
	        	return true;
	        }
	        return false;
		}
	}
	
	public static class ColorDesc {
	    public float[] components;
	    public String patternName;
	    public String colorSpace;
	    
		@Override
	    public boolean equals(Object obj) {
	        if (this == obj) {
	        	return true;
	        }
	        if (obj == null) {
	        	return false;
	        }
	        
	        ColorDesc aObj = (ColorDesc) obj;
	        if (this.patternName != aObj.patternName
	        		&& this.colorSpace != aObj.colorSpace) {
	        	return false;
	        }
	        
	        if (this.components != null && aObj.components != null) {
	        	if (this.components.length != aObj.components.length) {
	        		return false;
	        	}
	        	
	        	for (int i = 0; i < this.components.length; i++) {
	        		if (this.components[i] != aObj.components[i]) {
	        			return false;
	        		}
	        	}
	        }
	        return true;
		}
	}
	
	public PageContent() {
	}

	public void addOutlineShape(Shape shape) {
		if (this.outline == null) {
			this.outline = new ArrayList<Shape>();
		}
		this.outline.add(shape);
	}
	
	public List<Shape> getOutlineShapeList() {
		return this.outline;
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
    
    public int getY() {
    	Area area = this.getOutlineArea();
    	if (area == null) {
    		return 0;
    	}
    	return area.getBounds().y;
    }
    
    public int getX() {
    	Area area = this.getOutlineArea();
    	if (area == null) {
    		return 0;
    	}
    	return area.getBounds().x;
    }
    
    public int getHeight() {
    	Area area = this.getOutlineArea();
    	if (area == null) {
    		return 0;
    	}
    	return area.getBounds().height;
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
	
	public String getNonStrokingColorValue() {
		if (this.getGraphicsStateDesc() == null
				|| this.getGraphicsStateDesc().nonStrokingColor == null) {
			return null;
		}
		float[] components = this.getGraphicsStateDesc().nonStrokingColor.components;
		StringBuilder buf = new StringBuilder("");
		if (components != null) {
			for (float c : components) {
				buf.append(c + " ");
			}
		}
		return buf.toString();
	}
	
	public String getStrokingColorspace() {
		if (this.getGraphicsStateDesc() == null
				|| this.getGraphicsStateDesc().strokingColor == null) {
			return null;
		}
		return this.getGraphicsStateDesc().strokingColor.colorSpace;
	}
	
	public String getStrokingColorValue() {
		if (this.getGraphicsStateDesc() == null
				|| this.getGraphicsStateDesc().strokingColor == null) {
			return null;
		}
		float[] components = this.getGraphicsStateDesc().strokingColor.components;
		StringBuilder buf = new StringBuilder("");
		if (components != null) {
			for (float c : components) {
				buf.append(c + " ");
			}
		}
		return buf.toString();
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
	
	@Override
	public String toString() {
		return this.showString();
	}

	abstract public String showString();
	abstract public String getTypeString();
	
	abstract public Map<String, String> getAttrMap();
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

		public Rectangle getBBox(int begin, int end) {
			if (begin < 0 || end > this.outline.size()) {
				return new Rectangle(0, 0);
			}
			Area area = new Area();
	    	if (this.outline != null) {
	    		for (int i = begin; i < end; i++) {
	    			Shape s = this.outline.get(i);
	    			if (s instanceof GeneralPath) {
	    				area.add(new Area(((GeneralPath) s).getBounds()));
	    			} else {
	    				area.add(new Area(s));    				
	    			}
	        	}
	    	}
	    	return area.getBounds();
		}
	}
	
	public static class PathContent extends PageContent {

		private boolean fill;
		
		public PathContent(boolean fill) {
			super();
			this.type = Type.Path;
			this.fill = fill;
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

		public boolean isFill() {
			return this.fill;
		}
		
		public boolean merge(PathContent pathContent) {
			if (!this.getGraphicsStateDesc().equals(pathContent.getGraphicsStateDesc())) {
				return false;
			}
			
			Area a1 = this.getOutlineArea();
			Area a2 = pathContent.getOutlineArea();
			if (a1.getBounds().intersects(a2.getBounds())) {
				this.outline.addAll(pathContent.outline);
				return true;
			}
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

	}
	
	public static class AnnotContent extends PageContent {
		
		public String subType;
		public String fieldType;
		public String annotName;
		public String annotContents;
		
		private List<PageContent> appearenceContents;
		
		public AnnotContent() {
			super();
			this.type = Type.Annot;
			this.appearenceContents = new ArrayList<PageContent>();
		}
		
		public void addAppearanceContent(PageContent content) {
			this.appearenceContents.add(content);
		}
		
		public PageContent[] getAppearanceContents() {
			return this.appearenceContents.toArray(new PageContent[this.appearenceContents.size()]);
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
	}
}
