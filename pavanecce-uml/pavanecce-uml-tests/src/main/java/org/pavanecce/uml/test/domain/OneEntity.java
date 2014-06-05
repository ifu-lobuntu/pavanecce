package org.pavanecce.uml.test.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class OneEntity {
	@OneToMany(mappedBy = "one", cascade = CascadeType.ALL)
	private Set<ManyEntity> many;
	private String name;
	private Integer age;
	private AnEnum anEnum;

	public Set<ManyEntity> getMany() {
		return many;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public AnEnum getAnEnum() {
		return anEnum;
	}

	public void setAnEnum(AnEnum anEnum) {
		this.anEnum = anEnum;
	}

	public void setMany(Set<ManyEntity> many) {
		this.many = many;
	}

}
