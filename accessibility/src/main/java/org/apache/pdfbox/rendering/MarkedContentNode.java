package org.apache.pdfbox.rendering;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

public class MarkedContentNode {
	
    private Area area;
    private List<Shape> outline;
    private StringBuilder contentString;
    private String xObjectRefTag = null;
    
	private PDMarkedContent markedContent;
	private List<MarkedContentNode> children;
	
	public MarkedContentNode(PDMarkedContent content) {
		this.markedContent = content;
	}
	
	public void addMarkedContentNode(MarkedContentNode child) {
		if (this.children == null) {
			this.children = new ArrayList<MarkedContentNode>();
		}
		this.markedContent.addMarkedContent(child.markedContent);
		this.children.add(child);
	}
	
	public PDMarkedContent getMarkedContent() {
		return this.markedContent;
	}
	
	public void setXObjectRefTag(COSObject xobj) {
    	this.xObjectRefTag = createXObjectRefTag(xobj);
    }
    
    public String getXObjectRefTag() {
    	return this.xObjectRefTag;
    }

    public static final String createXObjectRefTag(COSObject obj) {
    	return obj.getObjectNumber() + "_" + obj.getGenerationNumber();
    }
    
    public boolean isArtifact() {
    	return false;
    }

    public void appendContentString(String contentString) {
    	if (this.contentString == null) {
    		this.contentString = new StringBuilder();
    	}
    	this.contentString.append(contentString);
    }
    
    public String getContentString() {
    	if (this.contentString != null) {
    		return this.contentString.toString();
    	} else if (this.markedContent.getContents() != null && !this.markedContent.getContents().isEmpty()) {
    		this.contentString = new StringBuilder();
    		for (Object obj : this.markedContent.getContents()) {
    			if (obj instanceof MarkedContentNode) {
    				this.contentString.append(((MarkedContentNode) obj).getContentString());    				
    			}
    		}
    		return this.contentString.toString();
    	}
    	return "";
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
        		this.area.add(new Area(s));
        	}    		
    	} else if (this.markedContent.getContents() != null && !this.markedContent.getContents().isEmpty()) {
    		this.contentString = new StringBuilder();
    		for (Object obj : this.markedContent.getContents()) {
    			if (obj instanceof MarkedContentNode) {
    				this.area.add(((MarkedContentNode) obj).getOutlineArea());
    			}
    		}
    	}
    	return this.area;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("tag=").append(markedContent.getTag())
            .append(", properties=").append(markedContent.getProperties());
        sb.append(", contents=").append(markedContent.getContents());
        
        if (markedContent.getAlternateDescription() != null) {
        	sb.append(", alt=" + markedContent.getAlternateDescription());
        }
        
       	sb.append(", content_string=" + this.getContentString());
        return sb.toString();
    }
}
