package org.pavanecce.eclipse.uml.reverse.db;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCForeignKey;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCTable;
import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.tables.BaseTable;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.pavanecce.eclipse.common.CommonEclipsePlugin;
import org.pavanecce.eclipse.uml.reverse.db.SelectedTableCollector;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PurgeDeletedRecordsAction extends Action {
	private List<PersistentTable> allTables = null;
	private Map<PersistentTable, List<ForeignKey>> foreinKeysByReferencedTable = new HashMap<PersistentTable, List<ForeignKey>>();
	private List<ForeignKey> allForeignKeys = null;
	private Collection<PersistentTable> selectedTables = null;
	private Collection<Schema> schemas;
	CharArrayWriter c = new CharArrayWriter();
	private PrintWriter out = new PrintWriter(c);
	private Collection<ForeignKey> droppedForeignKeys;

	@Override
	public void run() {
		init();
		new Job("Purge records marked for deletion") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Purge records marked for deletion", 1200);
					retrieveAllTables(new SubProgressMonitor(monitor, 200));
					retrieveAllForeignKeys(new SubProgressMonitor(monitor, 200));
					deleteRows(new SubProgressMonitor(monitor, 300));
					bulkCascadeSetNulOnDelete(new SubProgressMonitor(monitor, 300));
					recreateForeignKeys(new SubProgressMonitor(monitor, 150));
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							new Window(Display.getDefault().getActiveShell()) {
								protected Control createContents(Composite parent) {
									this.constrainShellSize();
									ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
									scrolledComposite.setLayoutData(new GridData(getShell().getBounds().width - 30, getShell().getBounds().height - 30));
									Text text2 = new Text(scrolledComposite, SWT.MULTI);
									scrolledComposite.setContent(text2);
									text2.setText(new String(c.toCharArray()));
									text2.setSize(text2.computeSize(SWT.DEFAULT, SWT.DEFAULT));
									return scrolledComposite;
								};
							}.open();
						}
					});
					return new Status(Status.OK, CommonEclipsePlugin.PLUGIN_ID, "Records deleted successfully");
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(Status.ERROR, CommonEclipsePlugin.PLUGIN_ID, "Deletion failed", e);
				} finally {
					monitor.done();
				}
			}
		}.schedule();
	}

	private void init() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelectionService selectionService = activeWorkbenchWindow.getSelectionService();
		IStructuredSelection selection = (IStructuredSelection) selectionService.getSelection();
		selectedTables = SelectedTableCollector.collectEffectivelySelectedTables(selection.iterator());
		if (selectedTables.size() > 0) {
			Schema s = selectedTables.iterator().next().getSchema();
			if (s.getDatabase() == null) {
				schemas = s.getCatalog().getSchemas();
			} else {
				schemas = s.getDatabase().getSchemas();
			}
		}
		droppedForeignKeys = new HashSet<ForeignKey>();
	}

	private void deleteRows(IProgressMonitor spm) {
		try {
			spm.beginTask("Deleting rows", selectedTables.size());
			spm.setTaskName("Deleting rows");
			for (PersistentTable jdbcTable : selectedTables) {
				spm.subTask(jdbcTable.getName());
				if (!spm.isCanceled() && isEntityTable(jdbcTable)) {
					dropReferencingForeignKeys(jdbcTable);
					deleteAssociationTables(jdbcTable);
					deleteSubTables(jdbcTable);
					List<ForeignKey> foreignKeys = jdbcTable.getForeignKeys();
					for (ForeignKey fk : foreignKeys) {
						if (isPartOfPrimaryKey(fk)) {
							deleteSuperTables(fk, fk);
							break;
						}
					}
					String sql = "delete from " + jdbcTable.getSchema().getName() + "." + jdbcTable.getName() + " where deleted_on < ?";
					executeDeleteByDeletedOnDate(jdbcTable, sql);
				}
				spm.worked(1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			spm.done();
		}
	}

	private void dropReferencingForeignKeys(PersistentTable jdbcTable) {
		List<ForeignKey> referencingForeignKeys = getReferencingForeignKeys(jdbcTable);
		for (ForeignKey fk : referencingForeignKeys) {
			if (!droppedForeignKeys.contains(fk)) {
				try {
					droppedForeignKeys.add(fk);
					PersistentTable baseTable = (PersistentTable) fk.getBaseTable();
					Connection jdbcConn = (Connection) ((JDBCTable) baseTable).getConnection();
					Statement st = jdbcConn.createStatement();
					String swl = "alter table " + fk.getBaseTable().getSchema().getName() + "." + fk.getBaseTable().getName() + " drop constraint " + fk.getName();
					out.println(swl);
					st.execute(swl);
					dropNotNullConstraints(fk);
				} catch (SQLException e) {
					CommonEclipsePlugin.logError("Database error occured", e);
				}
			}
		}
	}

	private boolean isEntityTable(BaseTable table) {
		EList columns = table.getColumns();
		boolean isEntityTable = false;
		for (Object object : columns) {
			if (((Column) object).getName().equalsIgnoreCase("deleted_on")) {
				isEntityTable = true;
				break;
			}
		}
		return isEntityTable;
	}

	private void deleteSuperTables(ForeignKey originalFk, ForeignKey currentFk) {
		PersistentTable superTable = getReferencedTable(currentFk);
		dropReferencingForeignKeys(superTable);
		deleteAssociationTables(superTable);
		String sql = "delete from " + superTable.getSchema().getName() + "." + superTable.getName() + " parent where (select deleted_on from "
				+ originalFk.getBaseTable().getSchema().getName() + "." + originalFk.getBaseTable().getName() + " child  where " + buildFKJoiningColumns(originalFk) + ") < ?";// Use
																																												// the
																																												// original
																																												// fk
																																												// as
																																												// it
																																												// should
																																												// only
																																												// be
																																												// referencing
																																												// 'id'
																																												// in
																																												// the
																																												// supertable
		executeDeleteByDeletedOnDate(superTable, sql);
		List<ForeignKey> foreignKeys = superTable.getForeignKeys();
		for (ForeignKey keyToParent : foreignKeys) {
			if (isPartOfPrimaryKey(keyToParent)) {
				deleteSuperTables(originalFk, keyToParent);
			}
		}
	}

	private boolean isPartOfPrimaryKey(ForeignKey currentFk) {
		boolean isFKToSuperTable = false;
		EList<Column> members = currentFk.getMembers();
		for (Column column : members) {
			if (column.isPartOfPrimaryKey()) {
				isFKToSuperTable = true;
				break;
			}
		}
		return isFKToSuperTable;
	}

	private void executeDeleteByDeletedOnDate(PersistentTable jdbcTable, String sql) {
		try {
			PreparedStatement st = ((JDBCTable) jdbcTable).getConnection().prepareStatement(sql);
			st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			out.println(sql);
			out.println(st.executeUpdate() + " records updated");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private List<ForeignKey> getReferencingForeignKeys(PersistentTable targetTable) {
		List<ForeignKey> result = foreinKeysByReferencedTable.get(targetTable);
		if (result == null) {
			result = new ArrayList<ForeignKey>();
			foreinKeysByReferencedTable.put(targetTable, result);
			for (ForeignKey jdbcForeignKey : allForeignKeys) {
				if (getReferencedTable(jdbcForeignKey).equals(targetTable)) {
					result.add(jdbcForeignKey);
				}
			}
		}
		return result;
	}

	private void dropNotNullConstraints(ForeignKey fk) {
		for (Column column : (List<Column>) fk.getMembers()) {
			if (!column.isNullable() && !column.isPartOfPrimaryKey()) {
				// Probably just an oversight - all foreign keys should be
				// nullable.
				try {
					String s = "alter table " + column.getTable().getSchema().getName() + "." + column.getTable().getName() + " alter column " + column.getName()
							+ " drop nullable ";
					out.println(s);
					Statement st = ((JDBCForeignKey) fk).getConnection().createStatement();
					out.println(st.executeUpdate(s) + " records updated");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void recreateForeignKeys(IProgressMonitor pm) {
		try {
			pm.beginTask("Recreating foreign key:", droppedForeignKeys.size());
			pm.setTaskName("Recreating foreign key:");
			for (ForeignKey fk : droppedForeignKeys) {
				try {
					pm.subTask(fk.getBaseTable().getName() + "->" + fk.getUniqueConstraint().getBaseTable().getName());
					PersistentTable baseTable = (PersistentTable) fk.getBaseTable();
					Connection jdbcConn = (Connection) ((JDBCTable) baseTable).getConnection();
					Statement st = jdbcConn.createStatement();
					PersistentTable referencedTable = getReferencedTable(fk);
					List<Column> referencedMembers = getReferencedColumns(fk);
					String create = "alter table " + baseTable.getSchema().getName() + "." + baseTable.getName() + " add constraint " + fk.getName() + " foreign key ("
							+ toCommaSeperatedString(fk.getMembers()) + " ) references " + referencedTable.getSchema().getName() + "." + referencedTable.getName() + "("
							+ toCommaSeperatedString(referencedMembers) + ")";
					st.execute(create);
					out.println(create);
					pm.worked(1);
				} catch (SQLException e) {
					CommonEclipsePlugin.logError("Database error occurred", e);
				}
			}
		} finally {
			pm.done();
		}
	}

	private PersistentTable getReferencedTable(ForeignKey fk) {
		PersistentTable referencedTable = null;
		if (fk.getReferencedTable() == null) {
			referencedTable = (PersistentTable) fk.getUniqueConstraint().getBaseTable();
		} else {
			referencedTable = (PersistentTable) fk.getReferencedTable();
		}
		return referencedTable;
	}

	private List<Column> getReferencedColumns(ForeignKey fk) {
		List<Column> referencedMembers = null;
		if (fk.getReferencedTable() == null) {
			referencedMembers = fk.getUniqueConstraint().getMembers();
		} else {
			referencedMembers = fk.getReferencedMembers();
		}
		return referencedMembers;
	}

	private String toCommaSeperatedString(List in) {
		List<Column> members = in;
		StringBuilder referringMembers = new StringBuilder();
		for (Column column : members) {
			referringMembers.append(column.getName());
			referringMembers.append(",");
		}
		referringMembers.delete(referringMembers.length() - 1, referringMembers.length());
		return referringMembers.toString();
	}

	private void retrieveAllTables(SubProgressMonitor pm) {
		if (allTables == null) {
			pm.beginTask("asdf", schemas.size());
			pm.setTaskName("Reading tables from schema: ");
			allTables = new ArrayList<PersistentTable>();
			for (Schema schema : schemas) {
				pm.subTask(schema.getName());
				for (Object object : schema.getTables()) {
					if (object instanceof PersistentTable) {
						allTables.add((PersistentTable) object);
					}
				}
				pm.worked(1);
			}
		}
		pm.done();
	}

	private void retrieveAllForeignKeys(IProgressMonitor pm) {
		if (allForeignKeys == null) {
			pm.beginTask("Collecting information about affected foreign keys", allTables.size());
			pm.setTaskName("Collecting information about foreign keys in table");
			allForeignKeys = new ArrayList<ForeignKey>();
			for (PersistentTable jdbcTable : allTables) {
				for (Object object : (List<?>) jdbcTable.getForeignKeys()) {
					pm.subTask(jdbcTable.getName());
					if (object instanceof ForeignKey) {
						allForeignKeys.add((ForeignKey) object);
					}
				}
				pm.worked(1);
			}
		}
		pm.done();
	}

	private void bulkCascadeSetNulOnDelete(IProgressMonitor pm) {
		try {
			pm.beginTask("Setting unmatched columns to null", droppedForeignKeys.size());
			pm.setTaskName("Setting unmatched columns to null");
			for (ForeignKey fk : droppedForeignKeys) {
				PersistentTable baseTable = (PersistentTable) fk.getBaseTable();
				pm.subTask(baseTable.getName() + "->" + getReferencedTable(fk).getName());
				if (!isPartOfPrimaryKey(fk)) {
					String s = buildBulkSetNullStatement(fk);
					executeUpdate(s, ((JDBCForeignKey) fk).getConnection());
				}
				pm.worked(1);
			}
		} finally {
			pm.done();
		}
	}

	private void deleteSubTables(PersistentTable parentTable) {
		for (ForeignKey fk : getReferencingForeignKeys(parentTable)) {
			if (isPartOfPrimaryKey(fk) && isEntityTable(fk.getBaseTable())) {
				PersistentTable childTable = (PersistentTable) fk.getBaseTable();
				dropReferencingForeignKeys(childTable);
				deleteSubTables(childTable);
				deleteAssociationTables(childTable);
				String sql = "delete from " + childTable.getSchema().getName() + "." + childTable.getName() + " child where (select deleted_on from "
						+ parentTable.getSchema().getName() + "." + parentTable.getName() + " parent where " + buildFKJoiningColumns(fk) + ") < ?";
				executeDeleteByDeletedOnDate(parentTable, sql);
			}
		}
	}

	private void deleteAssociationTables(PersistentTable parentTable) {
		for (ForeignKey fk : getReferencingForeignKeys(parentTable)) {
			if (isPartOfPrimaryKey(fk) && !isEntityTable(fk.getBaseTable())) {
				PersistentTable childTable = (PersistentTable) fk.getBaseTable();
				String sql = "delete from " + childTable.getSchema().getName() + "." + childTable.getName() + " child where (select deleted_on from "
						+ parentTable.getSchema().getName() + "." + parentTable.getName() + " parent  where " + buildFKJoiningColumns(fk) + ") < ?";
				executeDeleteByDeletedOnDate(parentTable, sql);
			}
		}
	}

	private void executeUpdate(String s, Connection connection) {
		try {
			out.println(s);
			Statement st = connection.createStatement();
			out.println(st.executeUpdate(s) + " records updated");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String buildBulkSetNullStatement(ForeignKey fk) {
		String s;
		PersistentTable rreferencedTable = getReferencedTable(fk);
		StringBuilder setNulls = new StringBuilder();
		List<Column> referencingColumns = fk.getMembers();
		for (Column column : referencingColumns) {
			setNulls.append(column.getName());
			setNulls.append("=null,");
		}
		StringBuilder isNotNull = new StringBuilder();
		for (int i = 0; i < referencingColumns.size(); i++) {
			isNotNull.append(" child.");
			isNotNull.append(referencingColumns.get(i).getName());
			isNotNull.append(" is not null ");
			if (i < referencingColumns.size() - 1) {
				isNotNull.append(" and ");
			}
		}
		setNulls.deleteCharAt(setNulls.length() - 1);
		s = "update " + fk.getBaseTable().getSchema().getName() + "." + fk.getBaseTable().getName() + " child set " + setNulls.toString() + " where "
				+ buildNoMatchFoundWithSubSelect(fk, rreferencedTable) + " and " + isNotNull;
		return s;
	}

	private String buildNoMatchFoundWithSubSelect(ForeignKey fk, PersistentTable referencedTable) {
		String string = buildFKJoiningColumns(fk);
		String notMatched = "0 = (select count(*) from " + referencedTable.getSchema().getName() + "." + referencedTable.getName() + " parent where " + string + ")";
		return notMatched;
	}

	private String buildFKJoiningColumns(ForeignKey fk) {
		String string = null;
		{
			StringBuilder joinColumns = new StringBuilder();
			List<Column> referencedColumnss = getReferencedColumns(fk);
			List<Column> referencingColumnss = fk.getMembers();
			for (int i = 0; i < referencedColumnss.size(); i++) {
				joinColumns.append(" child.");
				joinColumns.append(referencingColumnss.get(i).getName());
				joinColumns.append("=");
				joinColumns.append("parent.");
				joinColumns.append(referencedColumnss.get(i).getName());
				if (i < referencedColumnss.size() - 1) {
					joinColumns.append(" and ");
				}
			}
			string = joinColumns.toString();
		}
		return string;
	}
}