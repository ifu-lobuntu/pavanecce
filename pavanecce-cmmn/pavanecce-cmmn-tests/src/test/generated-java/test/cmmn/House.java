package test.cmmn;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ParentBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.common.collections.OneToManySet;

@Node(jcrType = "test:house", discriminator = false)
@Entity(name = "House")
@Table(name = "house")
public class House {
	@Bean(jcrName = "test:constructionCase", converter = ParentBeanConverterImpl.class)
	@OneToOne()
	@JoinColumns(value = { @JoinColumn(name = "construction_case_id", referencedColumnName = "id") })
	private ConstructionCase constructionCase = null;
	@Field(jcrName = "test:description", jcrType = "STRING")
	@Basic()
	@Column(name = "description")
	private String description = "";
	@Field(uuid = true)
	@Id()
	@GeneratedValue()
	private String id = null;
	@Bean(jcrName = "test:roofPlan", converter = ReferenceBeanConverterImpl.class)
	@OneToOne()
	@JoinColumns(value = { @JoinColumn(name = "roof_plan_id", referencedColumnName = "id") })
	private RoofPlan roofPlan = null;
	@SuppressWarnings("serial")
	private transient OneToManySet<House, WallPlan> wallPlansWrapper = new OneToManySet<House, WallPlan>(this) {
		public Set<WallPlan> getDelegate() {
			return wallPlans;
		}

		@SuppressWarnings("unchecked")
		protected OneToManySet<House, WallPlan> getChildren(House parent) {
			return (OneToManySet<House, WallPlan>) parent.getWallPlans();
		}

		public House getParent(WallPlan child) {
			return (House) child.getHouse();
		}

		public void setParent(WallPlan child, House parent) {
			child.zz_internalSetHouse(parent);
		}

		public boolean isLoaded() {
			return true;
		}

		public boolean isInstanceOfChild(Object o) {
			return o instanceof WallPlan;
		}
	};
	@Collection(jcrName = "test:wallPlans", collectionConverter = BeanReferenceCollectionConverterImpl.class)
	@OneToMany(mappedBy = "house")
	private Set<WallPlan> wallPlans = new HashSet<WallPlan>();
	@SuppressWarnings("serial")
	private transient OneToManySet<House, Wall> wallsWrapper = new OneToManySet<House, Wall>(this) {
		public Set<Wall> getDelegate() {
			return walls;
		}

		@SuppressWarnings("unchecked")
		protected OneToManySet<House, Wall> getChildren(House parent) {
			return (OneToManySet<House, Wall>) parent.getWalls();
		}

		public House getParent(Wall child) {
			return (House) child.getHouse();
		}

		public void setParent(Wall child, House parent) {
			child.zz_internalSetHouse(parent);
		}

		public boolean isLoaded() {
			return true;
		}

		public boolean isInstanceOfChild(Object o) {
			return o instanceof Wall;
		}
	};
	@Collection(jcrName = "test:walls", jcrElementName = "test:wall")
	@OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
	private Set<Wall> walls = new HashSet<Wall>();
	@Field(path = true)
	String path;
	@Field(jcrName = "test:uuid", jcrType = "String")
	@javax.persistence.Basic()
	private String uuid = getUuid();

	public House() {
	}

	public House(ConstructionCase owner) {
		this.setConstructionCase(owner);
	}

	public ConstructionCase getConstructionCase() {
		ConstructionCase result = this.constructionCase;
		return result;
	}

	public String getDescription() {
		String result = this.description;
		return result;
	}

	public String getId() {
		String result = this.id;
		return result;
	}

	public RoofPlan getRoofPlan() {
		RoofPlan result = this.roofPlan;
		return result;
	}

	public Set<WallPlan> getWallPlans() {
		Set<WallPlan> result = this.wallPlansWrapper;
		return result;
	}

	public Set<Wall> getWalls() {
		Set<Wall> result = this.wallsWrapper;
		return result;
	}

	public void setConstructionCase(ConstructionCase newConstructionCase) {
		ConstructionCase oldValue = this.constructionCase;
		if ((newConstructionCase == null || !(newConstructionCase.equals(oldValue)))) {
			this.constructionCase = newConstructionCase;
			if (!(oldValue == null)) {
				oldValue.setHouse(null);
			}
			if (!(newConstructionCase == null)) {
				if (!(this.equals(newConstructionCase.getHouse()))) {
					newConstructionCase.setHouse(this);
				}
			}
		}
	}

	public void setDescription(String newDescription) {
		this.description = newDescription;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRoofPlan(RoofPlan newRoofPlan) {
		RoofPlan oldValue = this.roofPlan;
		if ((newRoofPlan == null || !(newRoofPlan.equals(oldValue)))) {
			this.roofPlan = newRoofPlan;
			if (!(oldValue == null)) {
				oldValue.setHouse(null);
			}
			if (!(newRoofPlan == null)) {
				if (!(this.equals(newRoofPlan.getHouse()))) {
					newRoofPlan.setHouse(this);
				}
			}
		}
	}

	public void setWallPlans(Set<WallPlan> newWallPlans) {
		this.wallPlans = newWallPlans;
	}

	public void setWalls(Set<Wall> newWalls) {
		this.walls = newWalls;
	}

	public void zz_internalSetConstructionCase(ConstructionCase value) {
		this.constructionCase = value;
	}

	public void zz_internalSetRoofPlan(RoofPlan value) {
		this.roofPlan = value;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String value) {
		this.path = value;
	}

	public int hashCode() {
		return getUuid().hashCode();
	}

	public boolean equals(Object o) {
		return o instanceof House && ((House) o).getUuid().equals(getUuid());
	}

	public String getUuid() {
		if (uuid == null) {
			uuid = java.util.UUID.randomUUID().toString();
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
