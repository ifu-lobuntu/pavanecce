package org.pavanecce.eclipse.uml.reverse.java.jdt;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;

public class JdtAnnotated {

	private SourceAnnotation[] annotations;
	protected JavaDescriptorFactory factory;

	public JdtAnnotated(JavaDescriptorFactory factory) {
		super();
		this.factory=factory;
	}
	protected void init(IAnnotationBinding[] source){
		this.annotations = new SourceAnnotation[source.length];
		for (int i = 0; i < annotations.length; i++) {
			annotations[i] = new JavaJdtAnnotation(source[i], factory);
		}
	}

	public SourceAnnotation[] getAnnotations() {
		return annotations;
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