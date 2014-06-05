package org.pavanecce.uml.common.util.emulated;

import org.eclipse.uml2.uml.Element;

public interface IEmulatedElement extends Element {
	Element getOriginalElement();

	String getId();

	boolean shouldEmulate();
}
