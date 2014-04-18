package org.pavanecce.cmmn.test.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Entity
@Node(jcrType="t:house")
public class House {
	@Id
	@GeneratedValue()
	@Field(uuid=true)
	private String id;
	@OneToMany(cascade=CascadeType.ALL)
	@org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean
	@Collection(collectionConverter=ReferenceBeanConverterImpl.class)
	private Set<Wall> walls = new HashSet<Wall>();
	@Field()
	private String description;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<Wall> getWalls() {
		return walls;
	}

	public void setWalls(Set<Wall> walls) {
		this.walls = walls;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
