package org.pavanecce.eclilpse.uml.papyrus;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.MeasurementUnit;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.resource.sasheditor.DiModel;
import org.eclipse.papyrus.infra.core.resource.sasheditor.SashModel;
import org.eclipse.papyrus.infra.core.sashwindows.di.DiFactory;
import org.eclipse.papyrus.infra.core.sashwindows.di.PageRef;
import org.eclipse.papyrus.infra.core.sashwindows.di.SashWindowsMngr;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationModel;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.ModelEditPart;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.eclipse.uml.visualization.RelationshipDirection;
import org.pavanecce.eclipse.uml.visualization.RelationshipExtractor;

@SuppressWarnings("unchecked")
public class CreateClassDiagramNotationElementsCommand extends AbstractCommand {
	Collection<? extends Element> elements;
	NotationModel notationModel;
	DiModel diModel;
	private SashWindowsMngr sashWindowsMngr;
	private Set<Diagram> diagrams = new HashSet<Diagram>();
	RelationshipExtractor relationshipExtractor;
	ShapeBuilder shapeBuilder = new ShapeBuilder(ModelEditPart.MODEL_ID);
	private Map<Element, Shape> shapes = new HashMap<Element, Shape>();
	private String suffix;

	public CreateClassDiagramNotationElementsCommand(Collection<? extends Element> elements2, ModelSet modelSet, EClass[] relationshipTypes, RelationshipDirection d, String suffix) {
		super();
		relationshipExtractor = new RelationshipExtractor(relationshipTypes, d);
		this.elements = elements2;
		this.notationModel = (NotationModel) modelSet.getModel(NotationModel.MODEL_ID);
		Resource rst = ((SashModel) modelSet.getModel(DiModel.MODEL_ID)).getResource();
		this.sashWindowsMngr = (SashWindowsMngr) rst.getContents().get(0);
		this.suffix = suffix;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		Package pkg = getMostCommonPackage();
		Diagram dgm = NotationFactory.eINSTANCE.createDiagram();
		dgm.setType(ModelEditPart.MODEL_ID);
		dgm.setMeasurementUnit(MeasurementUnit.PIXEL_LITERAL);
		dgm.setElement(pkg);
		dgm.setName(NameConverter.capitalize(pkg.getName() + " " + suffix));
		notationModel.getResource().getContents().add(dgm);
		PageRef page = DiFactory.eINSTANCE.createPageRef();
		page.setEmfPageIdentifier(dgm);
		sashWindowsMngr.getPageList().getAvailablePage().add(page);
		diagrams.add(dgm);
		for (Element type : elements) {
			relationshipExtractor.doSwitch(type);
		}
		for (Element type : relationshipExtractor.getNodeElements()) {
			Shape shape = shapeBuilder.doSwitch(type);
			if (shape != null) {
				shapes.put(type, shape);
				dgm.getPersistedChildren().add(shape);
			}
		}
		ConnectorBuilder connectorBuilder = new ConnectorBuilder(ModelEditPart.MODEL_ID, shapes);
		for (Element r : relationshipExtractor.getRelationships()) {
			Collection<Connector> cs = connectorBuilder.doSwitch(r);
			if (cs != null) {
				for (Connector c : cs) {
					c.getSource().getDiagram().getPersistedEdges().add(c);
				}
			}
		}
		Map<Classifier, Integer> classifierCount = new HashMap<Classifier, Integer>();
		for (Element element : relationshipExtractor.getNodeElements()) {
			if (element instanceof Classifier) {
				int count = 0;
				Classifier cls = (Classifier) element;
				Set<Element> relationships = relationshipExtractor.getRelationships();
				for (Element relation : relationships) {
					if (relation instanceof Association) {
						Association ass = (Association) relation;
						if (ass.getEndTypes().contains(cls)) {
							count++;
						}
					}
				}
				classifierCount.put(cls, count);
			}
		}
		if (elements.size() == 1) {
			dgm.setName(((NamedElement) elements.iterator().next()).getName() + " " + suffix);
		} else {
			Entry<Classifier, Integer> max = null;
			for (Entry<Classifier, Integer> entry : classifierCount.entrySet()) {
				if (max == null || entry.getValue() > max.getValue()) {
					max = entry;
				}
			}
			dgm.setName(max.getKey().getName() + " " + suffix);
		}
	}

	private Package getMostCommonPackage() {
		Set<Package> pkgs = collectPackages();
		for (Package pkg2 : pkgs) {
			while (pkg2 != null) {
				if (containsAll(pkg2, pkgs)) {
					return pkg2;
				}
				pkg2 = pkg2.getNestingPackage();
			}
		}
		return null;
	}

	private boolean containsAll(Package potentialParent, Set<Package> pkgs) {
		for (Package p : pkgs) {
			if (!p.getQualifiedName().contains(potentialParent.getQualifiedName())) {
				return false;
			}
		}
		return true;
	}

	private Set<Package> collectPackages() {
		Set<Package> pkgs = new HashSet<Package>();
		for (Element element : elements) {
			if (element instanceof Package) {
				pkgs.add((Package) element);
			} else if (element.getNearestPackage() != null) {
				pkgs.add(element.getNearestPackage());
			} else {
				EObject container = element.eContainer();
				while (!(container instanceof Package || container == null)) {
					container = container.eContainer();
				}
				pkgs.add((Package) container);
			}
		}
		return pkgs;
	}

	@Override
	public boolean canExecute() {
		return true;
	}

	public Set<Diagram> getDiagrams() {
		return diagrams;
	}
}
