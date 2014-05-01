package org.pavanecce.common.code.metamodel.relationaldb;


public class RelationalInverseLink implements IRelationalElement{
	private String linkProperty;
	private boolean fromMany;
	private boolean toMany;
	private boolean isChild;
	public RelationalInverseLink(String linkProperty) {
		super();
		this.linkProperty = linkProperty;
	}
	public RelationalInverseLink(String linkProperty,boolean fromMany, boolean toMany, boolean isChild) {
		this(linkProperty);
		this.fromMany=fromMany;
		this.toMany=toMany;
		this.isChild=isChild;
	}
	public String getLinkProperty() {
		return linkProperty;
	}
	public boolean isManyToMany() {
		return fromMany && toMany;
	}
	public boolean isOneToMany() {
		return !fromMany && toMany;
	}
	public boolean isOneToOne() {
		return !fromMany && !toMany;
	}
	public boolean isChild() {
		return isChild;
	}
}
