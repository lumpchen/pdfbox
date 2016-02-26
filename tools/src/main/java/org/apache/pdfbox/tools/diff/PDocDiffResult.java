package org.apache.pdfbox.tools.diff;

import java.util.HashMap;
import java.util.Map;

public class PDocDiffResult {

	private Map<Integer, PageDiffResult> entrySet;
	private int diffPageCount;

	public PDocDiffResult() {
		this.entrySet = new HashMap<Integer, PageDiffResult>();
	}

	public void add(int pageNo, PageDiffResult pageResult) {
		this.entrySet.put(pageNo, pageResult);
		
		if (pageResult.count() > 0) {
			this.diffPageCount++;
		}
	}
	
	public int countOfDiffPages() {
		return this.diffPageCount;
	}
}
