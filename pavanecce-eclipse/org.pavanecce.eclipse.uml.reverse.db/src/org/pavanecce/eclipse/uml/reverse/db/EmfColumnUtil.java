package org.pavanecce.eclipse.uml.reverse.db;

import static org.pavanecce.uml.common.util.TagNames.*;

import java.util.List;

import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.constraints.UniqueConstraint;
import org.eclipse.datatools.modelbase.sql.datatypes.NumberDataType;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.common.util.PersistentNameUtil;

public class EmfColumnUtil {
	public static Property findTargetProperty(Property toOne, Column referencedColumn) {
		String referencedColumnName = referencedColumn.getName();
		return EmfPropertyUtil.findTargetPropertyByReferencedColumnName(toOne, referencedColumnName);
	}

	public static Property findAttribute(Column column, List<? extends Property> attributes, String expectedAttributeName) {
		Property found = null;
		for (Property property : attributes) {
			String name = PersistentNameUtil.getPersistentName(property);
			if (name.equals(column.getName())) {
				found = property;
				break;
			}
			if (property.getName().equalsIgnoreCase(expectedAttributeName)) {
				found = property;
				break;
			}
		}
		return found;
	}

	public static boolean isIdColumn(PersistentTable binding, Column c) {
		return binding.getPrimaryKey() != null && binding.getPrimaryKey().getMembers().size() == 1 && c.isPartOfPrimaryKey()
				&& ((Column) binding.getPrimaryKey().getMembers().get(0)).getName().equals("id") && (c.getDataType() instanceof NumberDataType);
	}

	public static Association findAssociation(Class fromClass, ForeignKey foreignKey) {
		Association ass = null;
		for (Association cur : fromClass.getAssociations()) {
			if (PersistentNameUtil.getPersistentName(cur).equals(foreignKey.getName())) {
				ass = cur;
				break;
			} else {
				Property to = findAssociationEnd(foreignKey, cur.getMemberEnds(), "");
				if(to!=null &&  to.getOtherEnd()==null){
					System.out.println();
				}
				if (to != null && to.getOtherEnd().getType().equals(fromClass)) {
					ass = cur;
					break;
				}
			}

		}
		return ass;
	}

	@SuppressWarnings("unchecked")
	public static Property findAssociationEnd(ForeignKey fk, List<? extends Property> attributes, String expectedAssociationEndName) {
		List<? extends Column> members = fk.getMembers();
		Property found = null;
		for (Property property : attributes) {
			for (Stereotype stereotype : property.getAppliedStereotypes()) {
				if (stereotype.getMember("linkPersistentName") != null) {
					if (fk.getName().equals(property.getValue(stereotype, "linkPersistentName"))) {
						found = property;
						break;
					}
				}
				if (stereotype.getMember(LINKED_PROPERTIES) != null) {
					if (columnsMatchLinkedProperties(members, property, stereotype)) {
						found = property;
						break;
					}
				}
			}
			if (found == null && property.getName().equalsIgnoreCase(expectedAssociationEndName)) {
				found = property;
				break;
			}
		}
		return found;
	}

	@SuppressWarnings("unchecked")
	private static boolean columnsMatchLinkedProperties(List<? extends Column> members, Property property, Stereotype stereotype) {
		EList<EObject> linkedProperties = (EList<EObject>) property.getValue(stereotype, LINKED_PROPERTIES);
		boolean isMatch = linkedProperties.size() > 0;
		if (linkedProperties.size() == members.size()) {
			for (int i = 0; i < members.size(); i++) {
				Column column = members.get(0);
				EObject lp = linkedProperties.get(0);
				String persistentName = (String) lp.eGet(lp.eClass().getEStructuralFeature(SOURCE_PERSISTENT_NAME));
				if (!column.getName().equals(persistentName)) {
					isMatch = false;
					break;
				}
			}
		}
		return isMatch;
	}

	@SuppressWarnings("unchecked")
	public static boolean isOneToOne(ForeignKey foreignKey) {
		boolean isOneToOne = false;
		List<? extends UniqueConstraint> uniqueConstraints = foreignKey.getBaseTable().getUniqueConstraints();
		for (UniqueConstraint uc : uniqueConstraints) {
			if (uc.getMembers().containsAll(foreignKey.getMembers())) {
				isOneToOne = true;
			}
		}
		return isOneToOne;
	}

}
