package org.pavanecce.uml.ocltocode.common;

import java.util.Comparator;

import org.eclipse.uml2.uml.Property;

public class CompareVarDeclsByType implements Comparator<Property> {

	public CompareVarDeclsByType() {
		super();
	}

	public int compare(Property var1, Property var2) {
		if (var1 != null && var2 != null) {
			return var1.getType().getName().compareTo(var2.getType().getName());
		}
		return 0;
	}

}
