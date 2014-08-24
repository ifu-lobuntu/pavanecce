package org.pavanecce.common.code.metamodel;

import java.util.HashMap;
import java.util.Map;

public class CodeElement implements Comparable<CodeElement> {
	protected String name;
	private String comment;
	private Map<Class<?>, Object> data;

	public CodeElement(String name2) {
		this.name = name2;
	}

	public <T, O extends T> void putData(Class<T> c, O o) {
		if (data == null) {
			data = new HashMap<Class<?>, Object>();
		}
		data.put(c, o);

	}

	@SuppressWarnings("unchecked")
	public <T> T getData(Class<T> c) {
		if (data == null) {
			return null;
		} else {
			return (T) data.get(c);
		}

	}

	public String getName() {
		return name;
	}

	@Override
	public int compareTo(CodeElement o) {
		if (this.name == null) {
			if (o.name == null) {
				return this.hashCode() - o.hashCode();
			} else {
				return -1;
			}
		} else {
			if (o.name == null) {
				return 1;
			} else {
				return this.name.compareTo(o.name);
			}
		}
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
