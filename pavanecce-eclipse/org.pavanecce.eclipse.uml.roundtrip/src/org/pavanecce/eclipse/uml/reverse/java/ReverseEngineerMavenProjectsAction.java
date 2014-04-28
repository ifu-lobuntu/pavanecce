package org.pavanecce.eclipse.uml.reverse.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.pavanecce.eclipse.common.CommonEclipsePlugin;
import org.pavanecce.eclipse.uml.reverse.pom.PackageGeneratorFromPoms;
import org.pavanecce.eclipse.uml.roundtrip.AbstractReverseEngineerAction;
import org.pavanecce.eclipse.uml.visualization.IDiagramCreator;
import org.pavanecce.eclipse.uml.visualization.RelationshipDirection;
import org.pavanecce.eclipse.uml.visualization.UmlVisualizationPlugin;

public class ReverseEngineerMavenProjectsAction extends AbstractReverseEngineerAction{

	public ReverseEngineerMavenProjectsAction(IStructuredSelection selection){
		super(selection,"Reverse Engineer Maven Projects to Packages");
	}
	@Override
	protected Command buildCommand(final Package model){
		try{
			final Collection<IContainer> types = selectContainers(selection);
			return new AbstractCommand(){
				@Override
				public boolean canExecute(){
					return true;
				}
				@Override
				public void execute(){
					ProgressMonitorDialog dlg = null;
					try{
						dlg = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
						dlg.setBlockOnOpen(false);
						dlg.open();
						Collection<Package> elements = new PackageGeneratorFromPoms((Model) model).generateUml(types, dlg.getProgressMonitor());
						Set<IDiagramCreator> diagramCreators = UmlVisualizationPlugin.getDefault().getDiagramCreators();
						for(IDiagramCreator c:diagramCreators){
							if(c.matches(selectedEditor)){
								c.createDiagram("Overview", elements, selectedEditor,RelationshipDirection.BOTH,UMLPackage.eINSTANCE.getPackageImport());
							}
						}
					}catch(Exception e){
						CommonEclipsePlugin.logError("Could not reverse Java classes", e);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Reverse Engineering Failed", e.toString());
					}finally{
						if(dlg != null){
							if(dlg.getProgressMonitor() != null){
								dlg.getProgressMonitor().done();
							}
							dlg.close();
						}
					}
				}
				@Override
				public void redo(){
				}
			};
		}catch(Exception e){
			CommonEclipsePlugin.logError("Could not reverse Java classes", e);
			return DO_NOTHING;
		}
	}
	private Collection<IContainer> selectContainers(IStructuredSelection selection){
		Object[] array = selection.toArray();
		Collection<IContainer> result=new HashSet<IContainer>();
		for(Object object:array){
			IResource r = null;
			if(object instanceof IResource){
				r = (IResource) object;
			}else if(object instanceof IAdaptable){
				IAdaptable a = (IAdaptable) object;
				r = (IResource) a.getAdapter(IResource.class);
			}
			
			if(r instanceof IContainer){
				IContainer c = (IContainer) r;
				IResource p = c.findMember("pom.xml");
				if(p!=null && p.exists()){
					result.add(c);
				}
			}
		}
		return result;
	}
}
