package org.pavanecce.uml.ocl2code.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ocl.uml.TupleType;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.uml.ocl2code.common.CompareVarDeclsByType;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;

public class TupleTypeMap extends ClassifierMap {
	private TupleType modelClass = null;

	public TupleTypeMap(UmlToCodeMaps CodeUtil, TupleType modelClass) {
		super(CodeUtil, modelClass);
		this.modelClass = modelClass;
	}

	/**
	 * @param in
	 * @return
	 */
	public String getClassName() {
		String[] typeNames = get_typenames();
		String result = "TupleType";
		for (int i = 0; i < typeNames.length; i++) {
			result = result + "_" + typeNames[i];
		}
		return result;
	}

	public String[] get_typenames() {
		Collection<Property> parts = modelClass.oclProperties();
		String[] typeNames = new String[parts.size()];
		Iterator<?> it = sort_parts().iterator();
		int j = 0;
		while (it.hasNext()) {
			Property var = (Property) it.next();
			// TODO remove all 'strange' characters from the typeName
			String name = var.getType().toString();
			name = name.replaceAll("\\(", "Of");
			name = name.replaceAll("\\)", "");
			typeNames[j++] = name;
		}
		return typeNames;
	}

	public Collection<Property> sort_parts() {
		Collection<Property> parts = modelClass.oclProperties();
		return sort(parts);
	}

	private Collection<Property> sort(Collection<Property> parts) {
		Comparator<Property> comp = new CompareVarDeclsByType();
		List<Property> sortedCollection = new ArrayList<Property>();
		sortedCollection.addAll(parts);
		Collections.sort(sortedCollection, comp);
		return sortedCollection;
	}

}
