package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;

public class JavaAnnotated {

	protected JavaDescriptorFactory factory;
	private SourceAnnotation[] annotations;

	public JavaAnnotated(JavaDescriptorFactory factory) {
		super();
		this.factory = factory;
	}

	protected void init(Annotation[] annotations) {
		this.annotations = new SourceAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			this.annotations[i] = new JavaReflectionAnnotation(annotations[i], factory);
		}
	}

	public SourceAnnotation[] getAnnotations() {
		return annotations;
	}

	protected static Annotation[] resolveAnnotations(Type type) {
		if (type instanceof AnnotatedElement) {
			Set<Annotation> result = new HashSet<Annotation>();
			for (Annotation annotation : ((AnnotatedElement) type).getAnnotations()) {
				if (!annotation.annotationType().equals(type)) {
					result.add(annotation);
				}
			}
			return result.toArray(new Annotation[result.size()]);
		}
		return new Annotation[0];
	}

	public SourceAnnotation getAnnotation(String string) {
		for (SourceAnnotation javaAnnotation : getAnnotations()) {
			if (javaAnnotation.getAnnotationType().getQualifiedName().equals(string)) {
				return javaAnnotation;
			}
		}
		return null;
	}

}