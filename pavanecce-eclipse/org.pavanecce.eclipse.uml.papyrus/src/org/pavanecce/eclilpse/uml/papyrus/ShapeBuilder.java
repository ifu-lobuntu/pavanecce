package org.pavanecce.eclilpse.uml.papyrus;

import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationClassAttributeCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationClassEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationClassNameEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.ClassAttributeCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.ClassEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.ClassNameEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.ModelEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.PackageEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.PackageNameEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.PackagePackageableElementCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.PropertyForClassEditPart;
import org.eclipse.uml2.uml.AssociationClass;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.util.UMLSwitch;

@SuppressWarnings("unchecked")
public class ShapeBuilder extends UMLSwitch<Shape> {
	private String diagramId;

	public ShapeBuilder(String diagramId) {
		this.diagramId = diagramId;
	}

	@Override
	public Shape casePackage(Package object) {
		if (this.diagramId.equals(ModelEditPart.MODEL_ID)) {
			Shape packageShape = NotationFactory.eINSTANCE.createShape();
			DecorationNode classNameDecorationNode = NotationFactory.eINSTANCE.createDecorationNode();
			packageShape.setType(PackageEditPart.VISUAL_ID + "");
			classNameDecorationNode.setType(PackageNameEditPart.VISUAL_ID + "");
			packageShape.getPersistedChildren().add(classNameDecorationNode);
			addCompartment(packageShape, PackagePackageableElementCompartmentEditPart.VISUAL_ID + "");
			packageShape.setElement(object);
			Bounds bnds = NotationFactory.eINSTANCE.createBounds();
			packageShape.setLayoutConstraint(bnds);
			bnds.setX(0);
			bnds.setY(0);
			bnds.setHeight(50);
			bnds.setWidth(300);
			return packageShape;
		} else {
			return super.casePackage(object);
		}
	}

	@Override
	public Shape caseClassifier(Classifier object) {
		if (this.diagramId.equals(ModelEditPart.MODEL_ID)) {
			Shape classShape = NotationFactory.eINSTANCE.createShape();
			DecorationNode classNameDecorationNode = NotationFactory.eINSTANCE.createDecorationNode();
			if (object instanceof AssociationClass) {
				classShape.setType(AssociationClassEditPart.VISUAL_ID + "");
				classNameDecorationNode.setType(AssociationClassNameEditPart.VISUAL_ID + "");
				classShape.getPersistedChildren().add(classNameDecorationNode);
				String id = AssociationClassAttributeCompartmentEditPart.VISUAL_ID + "";
				addAttributeCompartment(classShape, id, object);
//				addCompartment(classShape, AssociationClassOperationCompartmentEditPart.VISUAL_ID + "");
//				addCompartment(classShape, AssociationClassNestedClassifierCompartmentEditPart.VISUAL_ID + "");
			} else {
				classShape.setType(ClassEditPart.VISUAL_ID + "");
				classNameDecorationNode.setType(ClassNameEditPart.VISUAL_ID + "");
				classShape.getPersistedChildren().add(classNameDecorationNode);
				String id = ClassAttributeCompartmentEditPart.VISUAL_ID + "";
				addAttributeCompartment(classShape, id,object);
//				addCompartment(classShape, ClassOperationCompartmentEditPart.VISUAL_ID + "");
//				addCompartment(classShape, ClassNestedClassifierCompartmentEditPart.VISUAL_ID + "");
			}
			classShape.setElement(object);
			return classShape;
		} else {
			return super.caseClassifier(object);
		}
	}

	protected void addAttributeCompartment(Shape classShape, String id, Classifier object) {
		BasicCompartment attrComp = addCompartment(classShape, id);
		for (Property property : object.getAttributes()) {
			Shape propertyShape = NotationFactory.eINSTANCE.createShape();
			propertyShape.setElement(property);
			Bounds bnds = NotationFactory.eINSTANCE.createBounds();
			bnds.setX(0);
			bnds.setY(0);
//			bnds.setHeight(20);
			propertyShape.setLayoutConstraint(bnds);
			propertyShape.setType(PropertyForClassEditPart.VISUAL_ID+"");
			attrComp.getPersistedChildren().add(propertyShape);
			
		}
		Bounds bnds = NotationFactory.eINSTANCE.createBounds();
		bnds.setX(0);
		bnds.setY(0);
		bnds.setHeight((int)Math.ceil(object.getAttributes().size()*20)+20);
		bnds.setWidth(300);
		classShape.setLayoutConstraint(bnds);
	}

	private BasicCompartment addCompartment(Shape classShape, String value) {
		BasicCompartment nestedClassifierCompartment = NotationFactory.eINSTANCE.createBasicCompartment();
		nestedClassifierCompartment.setType(value);
		classShape.getPersistedChildren().add(nestedClassifierCompartment);
		nestedClassifierCompartment.getStyles().add(NotationFactory.eINSTANCE.createTitleStyle());
		nestedClassifierCompartment.getStyles().add(NotationFactory.eINSTANCE.createSortingStyle());
		nestedClassifierCompartment.getStyles().add(NotationFactory.eINSTANCE.createFilteringStyle());
		nestedClassifierCompartment.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		return nestedClassifierCompartment;
	}
}
