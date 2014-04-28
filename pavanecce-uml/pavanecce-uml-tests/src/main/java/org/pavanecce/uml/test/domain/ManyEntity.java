package org.pavanecce.uml.test.domain;

import javax.persistence.Entity;

@Entity
public class ManyEntity {
	private OneEntity one;

	public OneEntity getOne() {
		return one;
	}

	public void setOne(OneEntity one) {
		this.one = one;
	}

}
