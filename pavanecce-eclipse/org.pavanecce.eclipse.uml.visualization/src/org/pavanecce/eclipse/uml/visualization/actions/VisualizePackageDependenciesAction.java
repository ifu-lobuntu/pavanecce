package org.pavanecce.eclipse.uml.visualization.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.pavanecce.eclipse.common.AbstractEditingDomainAction;
import org.pavanecce.eclipse.common.AdapterFinder;
import org.pavanecce.eclipse.uml.visualization.IDiagramCreator;
import org.pavanecce.eclipse.uml.visualization.RelationshipDirection;
import org.pavanecce.eclipse.uml.visualization.UmlVisualizationPlugin;

public class VisualizePackageDependenciesAction extends AbstractEditingDomainAction{
	Set<Element> elements=new HashSet<Element>();
	public VisualizePackageDependenciesAction(IStructuredSelection selection){
		super(selection, "Visualize Package Dependencies");
		Object[] array = selection.toArray();
		for(Object object:array){
			EObject e = AdapterFinder.adaptObject(object);
			if(e instanceof Package){
				elements.add((Package) e);
			}
		}
	}
	@Override
	public void run(){
		Set<IDiagramCreator> diagramCreators = UmlVisualizationPlugin.getDefault().getDiagramCreators();
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		for(IDiagramCreator c:diagramCreators){
			if(c.matches(activeEditor)){
				c.createDiagram( "Package Dependencies", elements, activeEditor, RelationshipDirection.BOTH, UMLPackage.eINSTANCE.getPackageImport());
			}
		}
	}
}
