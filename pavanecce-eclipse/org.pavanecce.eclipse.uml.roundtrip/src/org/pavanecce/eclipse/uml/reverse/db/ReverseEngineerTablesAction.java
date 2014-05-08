package org.pavanecce.eclipse.uml.reverse.db;

import java.util.Collection;

import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.pavanecce.eclipse.uml.roundtrip.AbstractReverseEngineerAction;
public class ReverseEngineerTablesAction extends AbstractReverseEngineerAction{
	public ReverseEngineerTablesAction(IStructuredSelection selection){
		super(selection, "Reverse Engineer Tables");
	}
	@Override
	public AbstractCommand buildCommand(final Package model){
		return new AbstractCommand(){
			@Override
			public boolean canExecute(){
				return true;
			}
			@Override
			public void redo(){
				new UmlGenerator().generateUml(calculateTables(), (Model) model);
			}
			@Override
			public void execute(){
				redo();
			}
		};
	}
	private Collection<PersistentTable> calculateTables(){
		return SelectedTableCollector.collectEffectivelySelectedTables(selection.iterator());
	}
}
