package org.pavanecce.uml.reverse.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;

public class SimpleUmlGenerator extends AbstractUmlGenerator {
	@Override
	public Collection<Element> generateUmlImpl(Set<SourceClass> selection, Package library, IProgressMonitor m) throws Exception {
		m.beginTask("Importing Java classes", selection.size());

		Collection<Element> result = new HashSet<Element>();
		for (SourceClass t : selection) {
			m.worked(1);
			if (!t.isAnnotation()) {
				// Only do annotations in profiles
				Classifier cls = factory.getClassifierFor(t);
				result.add(cls);
				factory.getMappedTypes().put(cls.getQualifiedName(), t.getQualifiedName());
				populateAttributes(library, cls, t);
				populateOperations(library, cls, t);
			}
		}

		m.done();
		return result;

	}

	@Override
	protected boolean canReverse(Package library) {
		return library instanceof Model;
	}

}
