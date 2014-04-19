package org.pavanecce.cmmn.test.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Entity
@Node(discriminator=false,jcrType="j:wallQuote")
public class WallQuote {
	@Id
	@GeneratedValue
	@Field(uuid=true)
	String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
