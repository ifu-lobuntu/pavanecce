package org.pavanecce.eclipse.uml.reverse.db;

import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
import org.pavanecce.common.util.NameConverter;

public class DefaultNameGenerator implements INameGenerator{
	@Override
	public String calcAssociationName(ForeignKey foreignKey){
		String typeName = calcTypeName(foreignKey.getBaseTable().getName());
		Column object = (Column) foreignKey.getMembers().get(0);
		return typeName + "_" + calcAttributeName(object);
	}
	protected String calcTypeName(String tableName){
		return NameConverter.capitalize(NameConverter.underscoredToCamelCase(tableName));
	}
	
	@Override
	public String calcTypeName(PersistentTable returnType){
		return calcTypeName(returnType.getName());
	}
	@Override
	public String calcPackagename(PersistentTable returnType){
		return returnType.getSchema().getName();
	}
	@Override
	public String calcAttributeName(Column c){
		String raw = c.getName();
		if(raw.endsWith("_id") && c.isPartOfForeignKey()){
			raw = raw.substring(0, raw.length() - 3);
		}
		String result = NameConverter.underscoredToCamelCase(raw);
		return result;
	}
	@Override
	public String calcAssociationEndName(PersistentTable table){
		return NameConverter.decapitalize(calcTypeName(table)) ;
	}
	@Override
	public String calcAssociationEndName(ForeignKey foreignKey){
		return calcAttributeName((Column) foreignKey.getMembers().get(0));
	}
	@Override
	public boolean isAssociation(PersistentTable table) {
		return false;
	}
}
