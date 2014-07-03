package test.cmmn;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.common.collections.ManyToManyCollection;
import org.pavanecce.common.collections.ManyToManySet;
import org.pavanecce.common.ocm.GrandParentBeanConverterImpl;

@Node(jcrType = "test:wallPlan", discriminator = false)
@Entity(name = "WallPlan")
@Table(name = "wall_plan")
public class WallPlan {
	@Bean(jcrName = "test:house", converter = ReferenceBeanConverterImpl.class)
	@ManyToOne()
	@JoinColumns(value = { @JoinColumn(name = "house_id", referencedColumnName = "id") })
	private House house = null;
	@Bean(jcrName = "test:housePlan", converter = GrandParentBeanConverterImpl.class)
	@ManyToOne()
	@JoinColumns(value = { @JoinColumn(name = "house_plan_id", referencedColumnName = "id") })
	private HousePlan housePlan = null;
	@Field(uuid = true)
	@Id()
	@GeneratedValue()
	private String id = null;
	@SuppressWarnings("serial")
	private transient ManyToManySet<WallPlan, RoomPlan> roomPlansWrapper = new ManyToManySet<WallPlan, RoomPlan>(this) {
		public Set<RoomPlan> getDelegate() {
			return roomPlans;
		}

		@SuppressWarnings("unchecked")
		protected ManyToManyCollection<RoomPlan, WallPlan> getOtherEnd(RoomPlan other) {
			return (ManyToManyCollection<RoomPlan, WallPlan>) other.getWallPlans();
		}

		public boolean isLoaded() {
			return true;
		}

		public boolean isInstanceOfChild(Object o) {
			return o instanceof RoomPlan;
		}
	};
	@Collection(jcrName = "test:roomPlans", collectionConverter = BeanReferenceCollectionConverterImpl.class)
	@ManyToMany(mappedBy = "wallPlans")
	private Set<RoomPlan> roomPlans = new HashSet<RoomPlan>();
	@Field(jcrName = "test:shortDescription", jcrType = "STRING")
	@Basic()
	@Column(name = "short_description")
	private String shortDescription = "";
	@Bean(jcrName = "test:wall", converter = ReferenceBeanConverterImpl.class)
	@OneToOne()
	@JoinColumns(value = { @JoinColumn(name = "wall_id", referencedColumnName = "id") })
	private Wall wall = null;
	@Field(path = true)
	String path;
	@Field(jcrName = "test:uuid", jcrType = "String")
	@javax.persistence.Basic()
	private String uuid = getUuid();

	public WallPlan() {
	}

	public WallPlan(HousePlan owner) {
		this.setHousePlan(owner);
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

	public Set<RoomPlan> getRoomPlans() {
		Set<RoomPlan> result = this.roomPlansWrapper;
		return result;
	}

	public String getShortDescription() {
		String result = this.shortDescription;
		return result;
	}

	public Wall getWall() {
		Wall result = this.wall;
		return result;
	}

	public void setHouse(House newHouse) {
		if (!(newHouse == null)) {
			newHouse.getWallPlans().add(this);
		} else {
			if (!(this.house == null)) {
				this.house.getWallPlans().remove(this);
			}
		}
		this.house = newHouse;
	}

	public void setHousePlan(HousePlan newHousePlan) {
		if (!(newHousePlan == null)) {
			newHousePlan.getWallPlans().add(this);
		} else {
			if (!(this.housePlan == null)) {
				this.housePlan.getWallPlans().remove(this);
			}
		}
		this.housePlan = newHousePlan;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRoomPlans(Set<RoomPlan> newRoomPlans) {
		this.roomPlans = newRoomPlans;
	}

	public void setShortDescription(String newShortDescription) {
		this.shortDescription = newShortDescription;
	}

	public void setWall(Wall newWall) {
		Wall oldValue = this.wall;
		if ((newWall == null || !(newWall.equals(oldValue)))) {
			this.wall = newWall;
			if (!(oldValue == null)) {
				oldValue.setWallPlan(null);
			}
			if (!(newWall == null)) {
				if (!(this.equals(newWall.getWallPlan()))) {
					newWall.setWallPlan(this);
				}
			}
		}
	}

	public void zz_internalSetHouse(House value) {
		this.house = value;
	}

	public void zz_internalSetHousePlan(HousePlan value) {
		this.housePlan = value;
	}

	public void zz_internalSetWall(Wall value) {
		this.wall = value;
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
		return o instanceof WallPlan && ((WallPlan) o).getUuid().equals(getUuid());
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
