package org.apache.pdfbox.accessibility.validator.matterhorn;

public class GraphicsCheckPoint extends CheckPoint {

	
	String FailureCondition = "Figure tag alternative or replacement text missing.";

	public GraphicsCheckPoint() {
		super();
	}

	@Override
	public boolean check() {
		return false;
	}
			
}
