package org.apache.pdfbox.accessibility.check;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.accessibility.check.matterhorn.CheckPoint;

public class CheckResult {

	private List<CheckPoint> results;
	private int failureCount = 0;
	
	public CheckResult() {
		this.results = new ArrayList<CheckPoint>();
	}
	
	public void addCheckPoint(CheckPoint checkPoint) {
		this.results.add(checkPoint);
	}
	
	public int getFailureCount() {
		return this.failureCount;
	}
}
