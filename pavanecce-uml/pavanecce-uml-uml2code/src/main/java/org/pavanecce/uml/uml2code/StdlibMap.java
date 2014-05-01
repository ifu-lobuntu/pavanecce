package org.pavanecce.uml.uml2code;

import static org.pavanecce.common.code.metamodel.CodeCollectionKind.BAG;
import static org.pavanecce.common.code.metamodel.CodeCollectionKind.ORDERED_SET;
import static org.pavanecce.common.code.metamodel.CodeCollectionKind.SEQUENCE;
import static org.pavanecce.common.code.metamodel.CodeCollectionKind.SET;
import static org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind.BOOLEAN;
import static org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind.INTEGER;
import static org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind.REAL;
import static org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind.STRING;

import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;

public class StdlibMap {
	// TODO check the values of these attributes
	public static final CollectionTypeReference javaCollectionType = new CollectionTypeReference(BAG);
	public static final CollectionTypeReference javaSetType = new CollectionTypeReference(SET);
	public static final CollectionTypeReference javaSequenceType = new CollectionTypeReference(SEQUENCE);
	public static final CollectionTypeReference javaOrderedSetType = new CollectionTypeReference(ORDERED_SET);
	public static final CollectionTypeReference javaBagType = new CollectionTypeReference(BAG);
	public static final PrimitiveTypeReference javaStringType = new PrimitiveTypeReference(STRING);
	public static final PrimitiveTypeReference javaIntegerType = new PrimitiveTypeReference(INTEGER);
	public static final PrimitiveTypeReference javaBooleanType = new PrimitiveTypeReference(BOOLEAN);
	public static final PrimitiveTypeReference javaRealType = new PrimitiveTypeReference(REAL);
	public static final PrimitiveTypeReference javaIntegerObjectType = new PrimitiveTypeReference(INTEGER);
	public static final PrimitiveTypeReference javaBooleanObjectType = new PrimitiveTypeReference(BOOLEAN);
	public static final PrimitiveTypeReference javaRealObjectType = new PrimitiveTypeReference(REAL);

	public static final CollectionTypeReference javaSetImplType = new CollectionTypeReference(SET);
	public static final CollectionTypeReference javaSequenceImplType = new CollectionTypeReference(SEQUENCE);
	public static final CollectionTypeReference javaOrderedSetImplType = new CollectionTypeReference(ORDERED_SET);
	public static final CollectionTypeReference javaBagImplType = new CollectionTypeReference(BAG);
	public static final CodeTypeReference Object = new CodeTypeReference(false, "Any");
	public static final CodeTypeReference Void = new CodeTypeReference(false, "void");
	public static final CodeTypeReference Bool = javaBooleanObjectType;

	/**
	 * 
	 */
	public StdlibMap() {
		super();
		// TODO Auto-generated constructor stub
	}

}
