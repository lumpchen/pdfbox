package org.apache.pdfbox.accessibility;

import java.util.ArrayList;
import java.util.List;

public class CheckResult {

	private List<CheckPoint> results;
	private int failureCount = 0;
	
	public CheckResult() {
		this.results = new ArrayList<CheckPoint>();
	}
	
	public void addCheckPoint(CheckPoint checkPoint) {
		if (checkPoint.isFailure()) {
			this.failureCount++;
		}
		this.results.add(checkPoint);
	}
	
	public int getFailureCount() {
		return this.failureCount;
	}
}
