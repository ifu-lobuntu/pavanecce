package org.pavanecce.eclipse.uml.reverse.db;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.datatools.modelbase.sql.schema.Catalog;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;

public class SelectedTableCollector {
	public static Collection<PersistentTable> collectEffectivelySelectedTables(Iterator<?> iterator) {
		Collection<PersistentTable> result = new HashSet<PersistentTable>();
		while (iterator.hasNext()) {
			addTablesOnly(result, iterator.next());
		}
		return result;
	}

	private static void addTablesOnly(Collection<PersistentTable> result, Object object) {
		if (object instanceof PersistentTable) {
			result.add((PersistentTable) object);
		} else if (object instanceof Schema) {
			for (Object object2 : ((Schema) object).getTables()) {
				addTablesOnly(result, object2);
			}
		} else if (object instanceof Catalog) {
			for (Object jdbcSchema : ((Catalog) object).getSchemas()) {
				addTablesOnly(result, jdbcSchema);
			}
		}
	}
}
