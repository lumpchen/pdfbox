package org.apache.pdfbox.tools.diff.name.fraser.neil.plaintext;

import java.util.LinkedList;

import org.apache.pdfbox.tools.diff.name.fraser.neil.plaintext.diff_match_patch.Diff;

public class Test {

	public static void main(String[] args) {
		String s1 = "The difference algorithm in this library operates in character mode. "
				+ "This produces the most detailed diff possible. However, "
				+ "for some  one may wish to take a coarser approach.";
		
		String s2 = "applications The difference algorithm in this library operates in character mode. "
				+ "This produces the most detailed diff possible. However, "
				+ "for some applications one may wish to take a coarser approach.";
		
		LinkedList<Diff> diffs = (new diff_match_patch()).diff_main(s1, s2);
		for (Diff diff : diffs) {
			System.out.println(diff);
		}
	}
}
