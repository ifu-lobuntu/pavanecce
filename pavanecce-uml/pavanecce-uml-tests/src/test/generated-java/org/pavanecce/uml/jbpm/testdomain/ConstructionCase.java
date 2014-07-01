package org.pavanecce.uml.jbpm.testdomain;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "ConstructionCase")
@Table(name = "construction_case")
public class ConstructionCase {
	@Basic()
	@Column(name = "active")
	private Boolean active = false;
	@OneToOne(mappedBy = "constructionCase", cascade = CascadeType.ALL)
	private House house = null;
	@OneToOne(mappedBy = "constructionCase", cascade = CascadeType.ALL)
	private HousePlan housePlan = null;
	@Id()
	@GeneratedValue()
	private String id = null;
	@Basic()
	@Column(name = "name")
	private String name = "";
	@Basic()
	@Column(name = "number_of_walls")
	private Integer numberOfWalls = 0;
	private String path = "/ConstructionCaseCollection";
	@Lob()
	@Column(name = "picture")
	private byte[] picture = null;
	@Basic()
	@Column(name = "price_per_square_metre")
	private Double pricePerSquareMetre = 0.0;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate = null;

	public ConstructionCase() {
	}

	public ConstructionCase(String path) {
		this.path = path;
	}

	public Boolean getActive() {
		Boolean result = this.active;
		return result;
	}

	public House getHouse() {
		House result = this.house;
		return result;
	}

	public HousePlan getHousePlan() {
		HousePlan result = this.housePlan;
		return result;
	}

	public String getId() {
		String result = this.id;
		return result;
	}

	public String getName() {
		String result = this.name;
		return result;
	}

	public Integer getNumberOfWalls() {
		Integer result = this.numberOfWalls;
		return result;
	}

	public String getPath() {
		String result = this.path;
		return result;
	}

	public byte[] getPicture() {
		byte[] result = this.picture;
		return result;
	}

	public Double getPricePerSquareMetre() {
		Double result = this.pricePerSquareMetre;
		return result;
	}

	public Date getStartDate() {
		Date result = this.startDate;
		return result;
	}

	public void setActive(Boolean newActive) {
		this.active = newActive;
	}

	public void setHouse(House newHouse) {
		House oldValue = this.house;
		if ((newHouse == null || !(newHouse.equals(oldValue)))) {
			this.house = newHouse;
			if (!(oldValue == null)) {
				oldValue.setConstructionCase(null);
			}
			if (!(newHouse == null)) {
				if (!(this.equals(newHouse.getConstructionCase()))) {
					newHouse.setConstructionCase(this);
				}
			}
		}
	}

	public void setHousePlan(HousePlan newHousePlan) {
		HousePlan oldValue = this.housePlan;
		if ((newHousePlan == null || !(newHousePlan.equals(oldValue)))) {
			this.housePlan = newHousePlan;
			if (!(oldValue == null)) {
				oldValue.setConstructionCase(null);
			}
			if (!(newHousePlan == null)) {
				if (!(this.equals(newHousePlan.getConstructionCase()))) {
					newHousePlan.setConstructionCase(this);
				}
			}
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void setNumberOfWalls(Integer newNumberOfWalls) {
		this.numberOfWalls = newNumberOfWalls;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setPicture(byte[] newPicture) {
		this.picture = newPicture;
	}

	public void setPricePerSquareMetre(Double newPricePerSquareMetre) {
		this.pricePerSquareMetre = newPricePerSquareMetre;
	}

	public void setStartDate(Date newStartDate) {
		this.startDate = newStartDate;
	}

	public void zz_internalSetHouse(House value) {
		this.house = value;
	}

	public void zz_internalSetHousePlan(HousePlan value) {
		this.housePlan = value;
	}

	public void zz_internalSetPicture(byte[] value) {
		this.picture = value;
	}

	public void zz_internalSetStartDate(Date value) {
		this.startDate = value;
	}
}
