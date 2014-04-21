package org.pavanecce.cmmn.test.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ParentBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Entity
@Node(jcrType = "t:house", discriminator = false)
public class House {
	@Id
	@GeneratedValue()
	@Field(uuid = true)
	private String id;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "house",orphanRemoval=true)
	@Collection(jcrName = "t:walls", jcrElementName = "t:wall")
	private Set<Wall> walls = new HashSet<Wall>();
	@Field(jcrName = "t:description")
	private String description;
	@Bean(converter = ParentBeanConverterImpl.class)
	@OneToOne(mappedBy = "house")
	private ConstructionCase constructionCase;
	@Field(path = true)
	String path;
	@Bean(converter=ReferenceBeanConverterImpl.class,jcrName = "t:roofPlan")
	@OneToOne()
	private RoofPlan roofPlan;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="housePlan")
	@Collection(jcrName="t:wallPlans", collectionConverter=BeanReferenceCollectionConverterImpl.class)
	private Set<WallPlan> wallPlans = new HashSet<WallPlan>();
	public House() {

	}

	public House(ConstructionCase c) {
		constructionCase = c;
		c.setHouse(this);
	}

	public ConstructionCase getConstructionCase() {
		return constructionCase;
	}

	public void setConstructionCase(ConstructionCase constructionCase) {
		this.constructionCase = constructionCase;
	}

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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public RoofPlan getRoofPlan() {
		return roofPlan;
	}

	public void setRoofPlan(RoofPlan roofPlan) {
		this.roofPlan = roofPlan;
	}

	public Set<WallPlan> getWallPlans() {
		return wallPlans;
	}

	public void setWallPlans(Set<WallPlan> wallPlans) {
		this.wallPlans = wallPlans;
	}

}
