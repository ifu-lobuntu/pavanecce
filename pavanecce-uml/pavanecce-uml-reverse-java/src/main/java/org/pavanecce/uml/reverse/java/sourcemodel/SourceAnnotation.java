package org.pavanecce.uml.reverse.java.sourcemodel;

import java.util.Map.Entry;
import java.util.Set;

public interface SourceAnnotation {

	public abstract SourceClass getAnnotationType();

	public abstract Object getAnnotationAttribute(String string);

	public abstract Set<Entry<String, Object>> getMemberValuePairs();

}