package org.pavanecce.common.util;

import java.beans.Introspector;
import java.util.HashSet;
import java.util.Set;

public class NameConverter {
	private static Set<String> reservedWords = new HashSet<String>();
	static {
		// Python
		reservedWords.add("and");
		reservedWords.add("del");
		reservedWords.add("from");
		reservedWords.add("not");
		reservedWords.add("while");
		reservedWords.add("as");
		reservedWords.add("elif");
		reservedWords.add("global");
		reservedWords.add("or");
		reservedWords.add("with");
		reservedWords.add("assert");
		reservedWords.add("else");
		reservedWords.add("if");
		reservedWords.add("pass");
		reservedWords.add("yield");
		reservedWords.add("break");
		reservedWords.add("except ");
		reservedWords.add("import");
		reservedWords.add("print");
		reservedWords.add("class");
		reservedWords.add("exec");
		reservedWords.add("in");
		reservedWords.add("raise");
		reservedWords.add("continue");
		reservedWords.add("finally");
		reservedWords.add("is");
		reservedWords.add("return");
		reservedWords.add("def");
		reservedWords.add("for");
		reservedWords.add("lambda");
		reservedWords.add("try");
		// Java
		reservedWords.add("abstract");
		reservedWords.add("assert");
		reservedWords.add("boolean");
		reservedWords.add("break");
		reservedWords.add("byte");
		reservedWords.add("case");
		reservedWords.add("catch");
		reservedWords.add("char");
		reservedWords.add("class");
		reservedWords.add("const");
		reservedWords.add("continue");

		reservedWords.add("default");
		reservedWords.add("do");
		reservedWords.add("double");
		reservedWords.add("else");
		reservedWords.add("enum");
		reservedWords.add("extends");
		reservedWords.add("false");
		reservedWords.add("final");
		reservedWords.add("finally");
		reservedWords.add("float");
		reservedWords.add("for");

		reservedWords.add("goto");
		reservedWords.add("if");
		reservedWords.add("implements");
		reservedWords.add("import");
		reservedWords.add("instanceof");
		reservedWords.add("int");
		reservedWords.add("interface");
		reservedWords.add("long");
		reservedWords.add("native");
		reservedWords.add("new");
		reservedWords.add("null");

		reservedWords.add("package");
		reservedWords.add("private");
		reservedWords.add("protected");
		reservedWords.add("public");
		reservedWords.add("return");
		reservedWords.add("short");
		reservedWords.add("static");
		reservedWords.add("strictfp");
		reservedWords.add("super");
		reservedWords.add("switch");

		reservedWords.add("synchronized");
		reservedWords.add("this");
		reservedWords.add("throw");
		reservedWords.add("throws");
		reservedWords.add("transient");
		reservedWords.add("true");
		reservedWords.add("try");
		reservedWords.add("void");
		reservedWords.add("volatile");
		reservedWords.add("while");
		// Java
	}
	private static final String SINGULAR_SUFFIX = "ButOnlyOne";

	public static String toUpperCase(String name) {
		if (name == null) {
			return null;
		} else {
			return name.toUpperCase();
		}
	}

	public static String toLowerCase(String name) {
		if (name == null) {
			return null;
		} else {
			return name.toLowerCase();
		}
	}

	public static String deleteChar(String s, char c) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != c) {
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}

	public static String toUnderscoreStyle(String name) {
		if (name == null) {
			return null;
		}
		name = separateWordsToCamelCase(name);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			boolean upperLower = name.length() > i + 1 && Character.isLowerCase(name.charAt(i + 1)) && Character.isUpperCase(name.charAt(i)) && i > 0;
			boolean lowerUpper = i > 0 && Character.isLowerCase(name.charAt(i - 1)) && Character.isUpperCase(name.charAt(i));
			if ((upperLower || lowerUpper)) {
				// if(sb.length() == 0 || sb.charAt(sb.length() - 1) != '_'){
				// avoid duplicate underscores
				sb.append('_');
				// }
			}
			sb.append(name.charAt(i));
		}
		return sb.toString().toLowerCase();
	}

	/**
	 * If underscoreName is one of the following "abcDfg", "abc_dfg", "_abc_dfg", "AbcDfg" it will return "abcDfg"
	 * 
	 */
	public static String underscoredToCamelCase(String name) {
		if (name == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) != '_') {
				if (i > 0 && name.charAt(i - 1) == '_' && sb.length() > 0) {
					sb.append(Character.toUpperCase(name.charAt(i)));
				} else if (i == 0) {
					sb.append(Character.toLowerCase(name.charAt(i)));
				} else {
					sb.append(name.charAt(i));
				}
			}
		}
		return sb.toString();
	}

	public static String separateWords(String name) {
		if (name == null) {
			return null;
		}
		if (name.indexOf('_') == -1) {
			name = toUnderscoreStyle(name);
		}
		StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == '_') {
				sb.setCharAt(i, ' ');
			}
		}
		return sb.toString();
	}

	public static String capitalize(String name) {
		if (name == null) {
			return null;
		}
		char[] ca = name.toCharArray();
		if (ca.length == 0) {
			return name;
		} else {
			ca[0] = Character.toUpperCase(ca[0]);
			return new String(ca);
		}
	}

	public static String decapitalize(String name) {
		if (name == null) {
			return null;
		}
		return Introspector.decapitalize(name);
	}

	/**
	 * A very crude, primitive translation to plural in English. Should only have conversions that have an opposite
	 * conversion in toSingular. This is not intended for User Interfaces Postcondition: if name is not null, will
	 * always return a string with a different value than name. This guarrantee is required for code generation
	 * 
	 * @param name
	 * @return
	 */
	public static String toPlural(String name) {
		if (name == null) {
			return null;
		}
		if (name.endsWith("ay") || name.endsWith("ey")) {
			return name + "s";
		} else if (name.endsWith("y")) {
			return name.substring(0, name.length() - 1) + "ies";
		} else if (name.endsWith("o") || name.endsWith("s")) {
			return name + "es";
		} else if (name.endsWith("us")) {
			// Latin -us
			return name.substring(name.length() - 2) + "i";
		} else if (name.endsWith("hild")) {
			// "children" is used quite often
			return name + "ren";
		} else if (name.endsWith(SINGULAR_SUFFIX)) {
			return name.substring(0, name.length() - SINGULAR_SUFFIX.length());
		} else {
			return name + "s";
		}
	}

	/**
	 * A very crude, primitve translation to singular in English. Must be mirrored in toPlural(). Not intended for User
	 * interfaces. Postcondition: if name is not null, will always return a string with a different value than name.
	 * This guarrantee is required for code generation
	 * 
	 * @param name
	 * @return
	 */
	public static String toSingular(String name) {
		if (name == null) {
			return null;
		}
		if (name.endsWith("ays") || name.endsWith("eys")) {
			return name.substring(0, name.length() - 1);
		} else if (name.endsWith("ies")) {
			return name.substring(0, name.length() - 3) + "y";
		} else if (name.endsWith("ses") || name.endsWith("oes")) {
			return name.substring(0, name.length() - 2);
		} else if (name.endsWith("i")) {
			// Latin -us
			return name.substring(0, name.length() - 1) + "us";
		} else if (name.endsWith("hildren")) {
			return name.substring(0, name.length() - 3);
		} else if (name.endsWith("s")) {
			return name.substring(0, name.length() - 1);
		} else {
			return name + SINGULAR_SUFFIX;
		}
	}

	public static String separateWordsToCamelCase(String name) {
		if (name == null) {
			return null;
		}
		StringBuilder in = new StringBuilder(name);
		StringBuilder out = new StringBuilder();
		out.append(in.charAt(0));
		for (int i = 1; i < in.length(); i++) {
			if (Character.isJavaIdentifierPart(in.charAt(i))) {
				if (!Character.isJavaIdentifierPart(in.charAt(i - 1))) {
					out.append(Character.toUpperCase(in.charAt(i)));
				} else {
					out.append(in.charAt(i));
				}
			}
		}
		return out.toString().trim();
	}

	public static String toValidVariableName(String name) {
		if (reservedWords.contains(name)) {
			return "_" + name;
		} else {
			char[] charArray = null;
			if (name.length() > 0 && Character.isDigit(name.charAt(0))) {
				charArray = ("_" + name).toCharArray();
			} else {
				charArray = name.toCharArray();
			}
			for (int i = 0; i < charArray.length; i++) {
				if (!Character.isJavaIdentifierPart(charArray[i])) {
					charArray[i] = '_';
				}

			}
			return new String(charArray);
		}
	}

	public static String extractPropertyName(String name) {
		if (name.startsWith("is")) {
			// This is a standard across the system - we consider boolean property names as starting with "is"
			return decapitalize(name);
		} else if (name.startsWith("get") || name.startsWith("set")) {
			return decapitalize(name.substring(3));
		} else {
			return "";
		}
	}
}
