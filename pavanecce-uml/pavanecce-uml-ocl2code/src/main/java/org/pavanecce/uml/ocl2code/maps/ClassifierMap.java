package org.pavanecce.uml.ocl2code.maps;

import java.util.Collections;

import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.ocl.uml.TupleType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.PrimitiveType;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PrimitiveDefaultExpression;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.ocl2code.common.Check;
import org.pavanecce.uml.ocl2code.common.OclTypeNames;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.creators.OclUtilityCreator;
import org.pavanecce.uml.uml2code.StdlibMap;

public class ClassifierMap extends PackageableElementMap {
	protected Classifier elementType;
	protected CollectionKind collectionKind;

	public ClassifierMap(UmlToCodeMaps CodeUtil, Classifier modelClass) {
		super(CodeUtil, modelClass);
		if (modelClass instanceof CollectionType) {
			CollectionType ct = (CollectionType) modelClass;
			this.collectionKind = ct.getKind();
			this.elementType = ct.getElementType();
		} else {
			this.elementType = modelClass;
		}
		Check.pre("ClassifierMap constructor: modelClass is null", modelClass != null);
	}

	public boolean isAbstract() {
		return elementType.isAbstract();
	}

	public boolean isCollection() {
		return collectionKind != null;
	}

	public boolean isUmlPrimitive() {
		return elementType instanceof PrimitiveType;
	}

	public boolean isJavaPrimitive() {
		if (isUmlPrimitive()) {
			return getPrimitiveKind((PrimitiveType) elementType) != CodePrimitiveTypeKind.STRING;
		} else {
			return false;
		}
	}

	public CodeTypeReference javaTypePath() {
		return getJavaType(elementType, collectionKind);
	}

	public CodeTypeReference javaDefaultTypePath() {
		return getJavaImplType(elementType, collectionKind);
	}

	public CodeExpression defaultValue() {
		return getJavaDefault(elementType, collectionKind);
	}

	public CodeTypeReference javaElementTypePath() {
		if (collectionKind == null) {
			return null;
		} else {
			return codeUtil.classifierPathname(elementType);
		}
	}

	public CodeTypeReference javaObjectTypePath() {
		PrimitiveType type = (PrimitiveType) elementType;
		return getJavaObjectType(type);
	}

	private CodeTypeReference getJavaType(Classifier t, CollectionKind collectionKind) {
		CodeTypeReference result = null;
		if (t == null) {
			result = StdlibMap.Void;
		} else if (collectionKind != null) {
			result = getJavaColectionType(t, collectionKind);
		} else if (t instanceof Enumeration) {
			result = getJavaType((Enumeration) t);
		} else if (t instanceof PrimitiveType) {
			result = getJavaType((PrimitiveType) t);
		} else if (t instanceof TupleType) {
			result = getJavaType((TupleType) t);
		} else if (t instanceof DataType) {
			result = getJavaType((DataType) t);
		} else {
			result = pathname(t);
		}
		return result;
	}

	private CodeTypeReference getJavaColectionType(Classifier type, CollectionKind k) {
		CodeTypeReference result = getCollectionKind(k).getCopy();
		addElementType(type, result);
		return result;
	}

	private CodeTypeReference getJavaType(DataType t) {
		CodeTypeReference result = null;
		if (t.getName().equals(OclTypeNames.OclAnyTypeName)) {
			result = StdlibMap.Object;
		} else if (t.getName().equals(OclTypeNames.OclVoidTypeName)) {
			result = StdlibMap.Void;
		} else {
			result = pathname(t);
		}
		return result;
	}

	private CodeTypeReference getJavaType(Enumeration t) {
		CodeTypeReference result = null;
		result = pathname(t);
		return result;
	}

	private CodeTypeReference getJavaType(PrimitiveType t) {
		CodeTypeReference result = null;
		result = codeUtil.classifierPathname(t);
		if (EmfClassifierUtil.comformsToLibraryType(t, OclTypeNames.StringTypeName)) {
			result = StdlibMap.javaStringType;
		} else if (EmfClassifierUtil.comformsToLibraryType(t, OclTypeNames.RealTypeName)) {
			result = StdlibMap.javaRealObjectType;
		} else if (EmfClassifierUtil.comformsToLibraryType(t, OclTypeNames.IntegerTypeName)) {
			result = StdlibMap.javaIntegerObjectType;
		} else if (EmfClassifierUtil.comformsToLibraryType(t, OclTypeNames.BooleanTypeName)) {
			result = StdlibMap.javaBooleanObjectType;
		} else {
			result = pathname(t);
		}
		return result;
	}

	private CodeTypeReference getJavaType(TupleType t) {
		TupleTypeMap innerMap = codeUtil.buildTupleTypeMap(t);
		String name = innerMap.getClassName();
		return new CodeTypeReference(true, OclUtilityCreator.getTuplesPath().getCopy(), name, Collections.<String, String> emptyMap());
	}

	private CodeExpression getJavaDefault(Classifier t, CollectionKind k) {
		CodeExpression result = new NullExpression();
		if (k != null) {
			result = getJavaDefaultCollection(t, k);
		} else if (t instanceof Enumeration) {
			// result = getJavaDefault((Enumeration) t);
		} else if (t instanceof PrimitiveType) {
			result = getJavaDefault((PrimitiveType) t);
		} else if (t instanceof TupleType) {
			result = getJavaDefault((TupleType) t);
		} else if (t instanceof DataType) {
			result = getJavaDefault((DataType) t);
		}
		return result;
	}

	private CodeExpression getJavaDefaultCollection(Classifier t, CollectionKind k) {
		CodeTypeReference path = getJavaImplType(t, k);
		path = path.getCopy();
		addElementType(t, path);
		return new NewInstanceExpression(path);
	}

	private CodeExpression getJavaDefault(DataType t) {
		return new NullExpression();
	}

	private CodeExpression getJavaDefault(PrimitiveType t) {
		CodePrimitiveTypeKind kind = getPrimitiveKind(t);
		return new PrimitiveDefaultExpression(kind);
	}

	private CodePrimitiveTypeKind getPrimitiveKind(PrimitiveType t) {
		CodePrimitiveTypeKind kind = CodePrimitiveTypeKind.STRING;
		if (t.getName().equals(OclTypeNames.StringTypeName)) {
			kind = CodePrimitiveTypeKind.STRING;
		} else if (t.getName().equals(OclTypeNames.RealTypeName)) {
			kind = CodePrimitiveTypeKind.REAL;
		} else if (t.getName().equals(OclTypeNames.IntegerTypeName)) {
			kind = CodePrimitiveTypeKind.INTEGER;
		} else if (t.getName().equals(OclTypeNames.BooleanTypeName)) {
			kind = CodePrimitiveTypeKind.BOOLEAN;
		}
		return kind;
	}

	private CodeTypeReference getJavaImplType(Classifier type, CollectionKind t) {
		CodeTypeReference path = null;
		if (t != null) {
			if (t == CollectionKind.SET_LITERAL) {
				path = StdlibMap.javaSetImplType;
			} else if (t == CollectionKind.SEQUENCE_LITERAL) {
				path = StdlibMap.javaSequenceImplType;
			} else if (t == CollectionKind.BAG_LITERAL) {
				path = StdlibMap.javaBagImplType;
			} else if (t == CollectionKind.ORDERED_SET_LITERAL) {
				path = StdlibMap.javaOrderedSetImplType;
			}
			path = path.getCopy();
			addElementType(type, path);
		} else if (type instanceof PrimitiveType) {
			path = getJavaObjectType((PrimitiveType) type);
		} else if (type instanceof TupleType) {
			//
		} else {
			path = getJavaType(type, null);
		}
		return path;
	}

	private CodeTypeReference getJavaObjectType(PrimitiveType t) {
		CodeTypeReference result = null;
		if (t.getName().equals(OclTypeNames.StringTypeName)) {
			result = StdlibMap.javaStringType;
		} else if (t.getName().equals(OclTypeNames.RealTypeName)) {
			result = StdlibMap.javaRealObjectType;
		} else if (t.getName().equals(OclTypeNames.IntegerTypeName)) {
			result = StdlibMap.javaIntegerObjectType;
		} else if (t.getName().equals(OclTypeNames.BooleanTypeName)) {
			result = StdlibMap.javaBooleanObjectType;
		} else {
			result = pathname(t);
		}
		return result;
	}

	private CollectionTypeReference getCollectionKind(CollectionKind t) {
		CollectionTypeReference result = null;
		if (t == CollectionKind.SET_LITERAL) {
			result = StdlibMap.javaSetType;
		} else if (t == CollectionKind.SEQUENCE_LITERAL) {
			result = StdlibMap.javaSequenceType;
		} else if (t == CollectionKind.BAG_LITERAL) {
			result = StdlibMap.javaBagType;
		} else if (t == CollectionKind.ORDERED_SET_LITERAL) {
			result = StdlibMap.javaOrderedSetType;
		} else if (t == CollectionKind.COLLECTION_LITERAL) {
			result = StdlibMap.javaCollectionType;
		}
		return result;
	}

	private void addElementType(Classifier t, CodeTypeReference path) {
		CodeTypeReference innerPath = null;
		if (t instanceof PrimitiveType) {
			innerPath = getJavaObjectType((PrimitiveType) t);
		} else {
			innerPath = codeUtil.classifierPathname(t);
		}
		if (innerPath != null)
			path.addToElementTypes(innerPath);
	}

	protected CodeTypeReference pathname(Classifier t) {
		return codeUtil.classifierPathname(t);
	}

}
