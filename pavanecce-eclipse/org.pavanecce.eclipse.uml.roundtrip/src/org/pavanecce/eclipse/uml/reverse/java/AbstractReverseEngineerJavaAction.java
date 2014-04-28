package org.pavanecce.eclipse.uml.reverse.java;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.pavanecce.eclipse.common.CommonEclipsePlugin;
import org.pavanecce.eclipse.uml.reverse.java.jdt.JavaDescriptorFactory;
import org.pavanecce.eclipse.uml.roundtrip.AbstractReverseEngineerAction;
import org.pavanecce.eclipse.uml.visualization.IDiagramCreator;
import org.pavanecce.eclipse.uml.visualization.RelationshipDirection;
import org.pavanecce.eclipse.uml.visualization.UmlVisualizationPlugin;
import org.pavanecce.uml.reverse.java.AbstractUmlGenerator;
import org.pavanecce.uml.reverse.java.IProgressMonitor;
import org.pavanecce.uml.reverse.java.SourceClass;

public abstract class AbstractReverseEngineerJavaAction extends AbstractReverseEngineerAction {
	private final class DelegatingProgressMonitor implements IProgressMonitor {
		private final org.eclipse.core.runtime.IProgressMonitor dlg;

		private DelegatingProgressMonitor(org.eclipse.core.runtime.IProgressMonitor dlg) {
			this.dlg = dlg;
		}

		@Override
		public void worked(int i) {
			dlg.worked(i);
		}

		@Override
		public boolean isCanceled() {
			return dlg.isCanceled();
		}

		@Override
		public void done() {
			dlg.done();
		}

		@Override
		public void beginTask(String string, int size) {
			dlg.beginTask(string, size);
		}
	}

	public AbstractReverseEngineerJavaAction(IStructuredSelection selection, String name) {
		super(selection, name);
	}

	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}

	private static CompilationUnit parse(IClassFile unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}

	private Map<ITypeBinding, AbstractTypeDeclaration> selectTypeDeclarations() throws JavaModelException {
		Object[] selections = selection.toArray();
		Map<ITypeBinding, AbstractTypeDeclaration> types = new HashMap<ITypeBinding, AbstractTypeDeclaration>();
		for (Object object : selections) {
			addTypesIn(types, object);
		}
		return types;
	}

	protected void addTypesIn(Map<ITypeBinding, AbstractTypeDeclaration> types, Object object) throws JavaModelException {
		CompilationUnit cu = null;
		if (object instanceof ICompilationUnit) {
			cu = parse(((ICompilationUnit) object));
		} else if (object instanceof IClassFile) {
			cu = parse((IClassFile) object);
		} else if (object instanceof IPackageFragment) {
			IPackageFragment pf = (IPackageFragment) object;
			for (IJavaElement c : pf.getChildren()) {
				addTypesIn(types, c);
			}
		}
		if (cu != null) {
			@SuppressWarnings("unchecked")
			List<AbstractTypeDeclaration> typeDeclarations = cu.types();
			for (AbstractTypeDeclaration type : typeDeclarations) {
				types.put(type.resolveBinding(), type);
			}
		}
	}

	@Override
	protected Command buildCommand(final Package model) {
		return new AbstractCommand() {
			@Override
			public boolean canExecute() {
				return true;
			}

			@Override
			public void execute() {
				try {
					getRunnable(model).run(new NullProgressMonitor());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void redo() {
			}
		};
	}

	public IRunnableWithProgress getRunnable(final Package model) {
		return new IRunnableWithProgress() {

			@Override
			public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					final Map<ITypeBinding, AbstractTypeDeclaration> types = selectTypeDeclarations();
					JavaDescriptorFactory javaDescriptorFactory = new JavaDescriptorFactory();
					Set<SourceClass> descriptors = new HashSet<SourceClass>();
					for (Entry<ITypeBinding, AbstractTypeDeclaration> entry : types.entrySet()) {
						descriptors.add(javaDescriptorFactory.getClassDescriptor(entry.getKey(), entry.getValue()));
					}
					Collection<Element> elements = createGenerator().generateUml(descriptors, model, new DelegatingProgressMonitor(monitor));
					Set<IDiagramCreator> diagramCreators = UmlVisualizationPlugin.getDefault().getDiagramCreators();
					for (IDiagramCreator c : diagramCreators) {
						if (c.matches(selectedEditor)) {
							c.createDiagram(" Overview", elements, selectedEditor, RelationshipDirection.BOTH, UMLPackage.eINSTANCE.getAssociation());
						}
					}
				} catch (Exception e) {
					CommonEclipsePlugin.logError("Could not reverse Java classes", e);
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Reverse Engineering Failed", e.toString());
				} finally {
					monitor.done();
				}
			}
		};
	}

	protected abstract AbstractUmlGenerator createGenerator();
}
