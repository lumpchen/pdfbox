package org.apache.pdfbox.accessibility.tagged;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.accessibility.rendering.MarkedContentNode;
import org.apache.pdfbox.accessibility.rendering.PageStructureExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class DocumentStructureExtractor {
	private PDDocument document;
	private Map<Integer, List<MarkedContentNode>> markedContentMap;

	public DocumentStructureExtractor(PDDocument document) {
		this.document = document;
		this.markedContentMap = new HashMap<Integer, List<MarkedContentNode>>();
	}

	public List<MarkedContentNode> extract(int pageIndex) throws IOException {
		if (this.markedContentMap.containsKey(pageIndex)) {
			return this.markedContentMap.get(pageIndex);
		}
		PDPage page = this.document.getPage(pageIndex);
		if (page == null) {
			return null;
		}

		List<MarkedContentNode> contentList = this.extractStructure(page);
		this.markedContentMap.put(pageIndex, contentList);
		return contentList;
	}

	public List<MarkedContentNode> extract(PDPage page) throws IOException {
		int pageIndex = this.document.getPages().indexOf(page);
		return this.extract(pageIndex);
	}

	public List<MarkedContentNode> extractStructure(PDPage page) throws IOException {
		PageStructureExtractor pageExtractor = new PageStructureExtractor(page);
		pageExtractor.extract();
		return pageExtractor.getMarkedContentNodeList();
	}
}
