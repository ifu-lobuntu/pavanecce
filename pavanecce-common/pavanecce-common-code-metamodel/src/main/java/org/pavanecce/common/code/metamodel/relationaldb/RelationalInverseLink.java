package org.pavanecce.common.code.metamodel.relationaldb;


public class RelationalInverseLink implements IRelationalElement{
	private String linkProperty;

	public RelationalInverseLink(String linkProperty) {
		super();
		this.linkProperty = linkProperty;
	}
	public String getLinkProperty() {
		return linkProperty;
	}
}
