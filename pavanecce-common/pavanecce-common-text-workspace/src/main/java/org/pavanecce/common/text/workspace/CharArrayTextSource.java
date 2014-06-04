package org.pavanecce.common.text.workspace;

import java.io.CharArrayWriter;

public class CharArrayTextSource implements TextSource {
	CharArrayWriter writer;
	private char[] charArray;

	public CharArrayTextSource(CharArrayWriter contentWriter) {
		this.writer = contentWriter;
	}

	public CharArrayTextSource(char[] charArray) {
		this.charArray = charArray;
	}

	@Override
	public char[] toCharArray() {
		if (charArray != null) {
			return charArray;
		} else {
			return writer.toCharArray();
		}
	}

	@Override
	public boolean hasContent() {
		return true;
	}
}
