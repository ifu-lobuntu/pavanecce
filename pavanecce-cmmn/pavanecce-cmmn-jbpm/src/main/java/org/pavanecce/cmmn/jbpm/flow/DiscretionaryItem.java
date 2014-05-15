package org.pavanecce.cmmn.jbpm.flow;

public class DiscretionaryItem<T extends PlanItemDefinition> extends TableItem {
	private static final long serialVersionUID = 2371336993789669482L;
	private T definition;
	private String definitionRef;
	private PlanItemControl itemControl;
	private long id;

	public T getDefinition() {
		return definition;
	}

	public void setDefinition(T definition) {
		this.definition = definition;
	}

	public String getDefinitionRef() {
		return definitionRef;
	}

	public void setDefinitionRef(String definitionRef) {
		this.definitionRef = definitionRef;
	}

	public PlanItemControl getItemControl() {
		return itemControl;
	}

	public void setItemControl(PlanItemControl itemControl) {
		this.itemControl = itemControl;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

}
