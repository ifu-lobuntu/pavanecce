package org.pavanecce.common.util;

public class OclPrimitives {

	public OclPrimitives() {
	}

	public static String toUpper(String i) {
		return i.toUpperCase();
	}

	public static String toLower(String i) {
		return i.toLowerCase();
	}

	public static String concat(String i, String j) {
		return i + j;
	}

	public static String replaceAll(String source, String i, String j) {
		return source.replaceAll(i, j);
	}

	public static Integer length(String source) {
		return source.length();
	}
}
