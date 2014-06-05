package org.pavanecce.uml.common.util.emulated;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.TypedElement;
import org.pavanecce.uml.common.ocl.OclQueryContext;

public class EmulatedPropertyHolder implements IEmulatedPropertyHolder {
	private EList<AbstractEmulatedProperty> emulatedAttributes = new BasicEList<AbstractEmulatedProperty>();
	protected Classifier owner;
	protected Map<Element, OclQueryContext> queries = new HashMap<Element, OclQueryContext>();
	protected org.pavanecce.uml.common.util.emulated.IPropertyEmulation emulation;

	public EmulatedPropertyHolder(Classifier owner) {
		this.owner = owner;
	}

	public EmulatedPropertyHolder(Classifier owner2, IPropertyEmulation e, EList<? extends TypedElement>[] typedElements) {
		this(owner2, e);
		for (EList<? extends TypedElement> eList : typedElements) {
			for (TypedElement typedElement : eList) {
				addTypedElementBridge(typedElement);
			}
		}
	}

	protected void addTypedElementBridge(TypedElement te) {
		emulatedAttributes.add(new TypedElementPropertyBridge(owner, te, this.emulation));
	}

	public EmulatedPropertyHolder(Classifier owner2, IPropertyEmulation e) {
		this(owner2);
		this.emulation = e;
	}

	@Override
	public void putQuery(Element e, OclQueryContext exp) {
		queries.put(e, exp);
	}

	@Override
	public Collection<OclQueryContext> getQueries() {
		return queries.values();
	}

	protected void removeEmulatedAttribute(Element original) {
		Iterator<AbstractEmulatedProperty> iterator = emulatedAttributes.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getOriginalElement() == original) {
				iterator.remove();
				break;
			}
		}
	}

	@Override
	public Property getEmulatedAttribute(Element originalElement) {
		for (Property p : emulatedAttributes) {
			Element originalElement2 = ((IEmulatedElement) p).getOriginalElement();
			if (originalElement2 == originalElement) {
				return p;
			} else if (originalElement2 instanceof IEmulatedElement && ((IEmulatedElement) originalElement2).getOriginalElement() == originalElement) {
				return p;
			}
		}
		return null;
	}

	@Override
	public EList<AbstractEmulatedProperty> getEmulatedAttributes() {
		return emulatedAttributes;
	}

	@Override
	public void addEmulatedAttribute(AbstractEmulatedProperty otherEnd) {
		getEmulatedAttributes().add(otherEnd);
	}

}
