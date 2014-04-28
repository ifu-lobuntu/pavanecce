package org.pavanecce.eclipse.uml.reverse.db;

import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;

public class FusionNameGenerator extends DefaultNameGenerator {
	@Override
	public boolean isAssociation(PersistentTable table) {
		return table.getName().toLowerCase().startsWith("asso_");
	}
}
