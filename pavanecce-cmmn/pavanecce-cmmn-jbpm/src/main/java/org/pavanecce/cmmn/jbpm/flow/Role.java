package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

public class Role implements Serializable, CMMNElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8146649730480734851L;
	private String name;
	private String elementId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setElementId(String value) {
		this.elementId = value;

	}

	@Override
	public String getElementId() {
		return elementId;
	}
}
