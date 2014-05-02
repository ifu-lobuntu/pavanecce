package org.pavanecce.uml.uml2code;

import java.util.ArrayDeque;
import java.util.Deque;

public class AbstractTextGenerator {

	protected StringBuilder sb;
	protected Deque<StringBuilder> sbStack = new ArrayDeque<StringBuilder>();

	public AbstractTextGenerator() {
		super();
	}

	public AbstractTextGenerator append(String string) {
		sb.append(string);
		return this;
	}

	protected StringBuilder popStringBuilder() {
		StringBuilder sb;
		sb = sbStack.pop();
		this.sb = sbStack.peek();
		return sb;
	}

	protected StringBuilder pushNewStringBuilder() {
		StringBuilder sb = new StringBuilder();
		sbStack.push(sb);
		this.sb = sb;
		return sb;
	}

}