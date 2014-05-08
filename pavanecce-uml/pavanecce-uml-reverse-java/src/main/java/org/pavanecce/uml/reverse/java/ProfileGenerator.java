package org.pavanecce.uml.reverse.java;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;

public class ProfileGenerator extends AbstractUmlGenerator {
	@Override
	public Collection<Element> generateUmlImpl(Set<SourceClass> selection, Package library, IProgressMonitor m) throws Exception {
		m.beginTask("Import Java Annotations in Profile", selection.size());
		for (SourceClass t : selection) {
			m.worked(1);
			if (!m.isCanceled() && shouldReverseType(t)) {
				Classifier cls = factory.getClassifierFor(t);
				factory.getMappedTypes().put(cls.getQualifiedName(), t.getQualifiedName());
				populateAttributes(library, cls, t);
				populateOperations(library, cls, t);
			}
		}
		m.done();
		return Collections.emptySet();
	}

	@Override
	protected boolean canReverse(Package library) {
		return library instanceof Profile;
	}

	@Override
	protected Property createAttribute(Classifier cls, SourceProperty pd) {
		Property attr = super.createAttribute(cls, pd);
		if (pd.getBaseType().isAnnotation()) {
			attr.setAggregation(AggregationKind.COMPOSITE_LITERAL);
		}
		return attr;
	}

	protected boolean shouldReverseType(SourceClass t) {
		return t.isAnnotation() || t.isEnum();
	}

}
