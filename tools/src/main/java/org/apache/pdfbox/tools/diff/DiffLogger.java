package org.apache.pdfbox.tools.diff;

public class DiffLogger {
	
	public static DiffLogger getInstance() {
		return new DiffLogger();
	}
	
	public void info(String msg) {
		System.out.println(msg);
	}
	
	public void error(String msg) {
		System.err.println(msg);
	}
	
	public void warn(String msg) {
		System.err.println(msg);
	}
	
	public void error(Throwable t) {
		t.printStackTrace();
	}
}
