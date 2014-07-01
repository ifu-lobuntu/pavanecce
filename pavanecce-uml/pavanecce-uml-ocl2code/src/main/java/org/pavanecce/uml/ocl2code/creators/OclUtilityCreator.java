package org.pavanecce.uml.ocl2code.creators;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.TypeResolver;
import org.eclipse.ocl.uml.TupleType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;

public class OclUtilityCreator {
	static private CodePackageReference tuplesPath;
	static private CodePackage tuplesPackage;
	private UmlToCodeMaps codeMaps;

	public OclUtilityCreator(UmlToCodeMaps umlToCodeMaps, CodeModel javamodel, Element element) {
		super();
		this.codeMaps = umlToCodeMaps;
		tuplesPath = new CodePackageReference(umlToCodeMaps.utilPackagePath(element), "tuples", Collections.<String, String> emptyMap());
		tuplesPackage = javamodel.findOrCreatePackage(tuplesPath);

	}

	public void makeOclUtilities(TypeResolver<Classifier, Operation, Property> tr) {
		makeTupleTypes(tr);
	}

	private void makeTupleTypes(TypeResolver<Classifier, Operation, Property> tr) {
		// get the tupletypes from the standlib and transform these
		EList<Type> types = null;
		for (EObject o : tr.getResource().getContents()) {
			if (o instanceof Package) {
				Package pkg = (Package) o;
				if ("tuples".equals(pkg.getName())) {
					types = pkg.getOwnedTypes();
					break;
				}
			}
		}
		if (types != null) {
			Iterator<?> it = types.iterator();
			while (it.hasNext()) {
				TupleType tupletype = (TupleType) it.next();
				TupleTypeCreator tupleMaker = new TupleTypeCreator(codeMaps);
				tupleMaker.make(tupletype, tuplesPackage);
			}
		}

	}

	public static CodePackageReference getTuplesPath() {
		return tuplesPath.getCopy();
	}
}
