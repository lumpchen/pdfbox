package org.apache.pdfbox.accessibility.validate;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCheckProcess implements CheckProcess {

	protected List<CheckPoint> checkPointList;
	
	protected AbstractCheckProcess() {
		this.checkPointList = new ArrayList<CheckPoint>();
	}
	
	protected void addCheckPoint(CheckPoint checkPoint) {
		this.checkPointList.add(checkPoint);
	}
}
