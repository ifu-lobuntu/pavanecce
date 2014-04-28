package org.pavanecce.eclipse.uml.reverse.db;

import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
public interface INameGenerator{
	public abstract String calcAssociationEndName(PersistentTable table);
	public abstract String calcAssociationEndName(ForeignKey foreignKey);
	public abstract String calcAssociationName(ForeignKey foreignKey);
	public abstract String calcTypeName(PersistentTable returnType);
	public abstract String calcAttributeName(Column c);
	public abstract String calcPackagename(PersistentTable returnType);
	public abstract boolean isAssociation(PersistentTable table);
}
