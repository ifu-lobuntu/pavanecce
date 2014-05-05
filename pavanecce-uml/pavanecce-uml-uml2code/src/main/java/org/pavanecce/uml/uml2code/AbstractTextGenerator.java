package org.pavanecce.uml.uml2code;

import java.util.ArrayDeque;
import java.util.Deque;

public class AbstractTextGenerator {

	protected StringBuilder sb;
	protected Deque<StringBuilder> sbStack = new ArrayDeque<StringBuilder>();
	protected Deque<String> paddingStack = new ArrayDeque<String>();
	protected String padding;

	public AbstractTextGenerator() {
		super();
	}

	public AbstractTextGenerator append(String string) {
		sb.append(string);
		return this;
	}

	protected StringBuilder popStringBuilder() {
		StringBuilder sb = sbStack.pop();
		this.sb = sbStack.peek();
		return sb;
	}

	protected StringBuilder pushNewStringBuilder() {
		StringBuilder sb = new StringBuilder();
		sbStack.push(sb);
		this.sb = sb;
		return sb;
	}

	protected void popPadding() {
		paddingStack.pop();
		this.padding = paddingStack.peek();
	}

	protected void pushPadding(String padding) {
		paddingStack.push(padding);
		this.padding = padding;
	}

}