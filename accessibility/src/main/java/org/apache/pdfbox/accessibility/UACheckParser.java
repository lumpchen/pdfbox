package org.apache.pdfbox.accessibility;

import java.io.File;
import java.io.IOException;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;

public class UACheckParser extends PDFParser {

	protected DataSource dataSource;
	
	public UACheckParser(File file) throws IOException {
		// TODO move file handling outside of the parser
		super(new RandomAccessBufferedFileInputStream(file));
		this.setLenient(false);
		this.dataSource = new FileDataSource(file);
	}

	public UACheckParser(String filename) throws IOException {
		// TODO move file handling outside of the parser
		this(new File(filename));
	}

	public UACheckParser(DataSource dataSource) throws IOException {
		// TODO move file handling outside of the parser
		super(new RandomAccessBufferedFileInputStream(dataSource.getInputStream()));
		this.setLenient(false);
		this.dataSource = dataSource;
	}
}
