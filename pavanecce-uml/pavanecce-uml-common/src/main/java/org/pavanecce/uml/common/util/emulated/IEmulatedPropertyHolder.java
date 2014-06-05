package org.pavanecce.uml.common.util.emulated;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.uml.common.ocl.OclQueryContext;

public interface IEmulatedPropertyHolder {
	Property getEmulatedAttribute(Element originalElement);

	EList<AbstractEmulatedProperty> getEmulatedAttributes();

	void addEmulatedAttribute(AbstractEmulatedProperty otherEnd);

	void putQuery(Element e, OclQueryContext ctx);

	Collection<OclQueryContext> getQueries();
}
