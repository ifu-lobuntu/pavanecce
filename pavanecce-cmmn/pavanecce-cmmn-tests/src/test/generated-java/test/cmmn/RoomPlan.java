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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.common.collections.ManyToManyCollection;
import org.pavanecce.common.collections.ManyToManySet;
import org.pavanecce.common.ocm.GrandParentBeanConverterImpl;

@Node(jcrType = "test:roomPlan", discriminator = false)
@Entity(name = "RoomPlan")
@Table(name = "room_plan")
public class RoomPlan {
	@Bean(jcrName = "test:housePlan", converter = GrandParentBeanConverterImpl.class)
	@ManyToOne()
	@JoinColumns(value = { @JoinColumn(name = "house_plan_id", referencedColumnName = "id") })
	private HousePlan housePlan = null;
	@Field(uuid = true)
	@Id()
	@GeneratedValue()
	private String id = null;
	@Field(jcrName = "test:name", jcrType = "STRING")
	@Basic()
	@Column(name = "name")
	private String name = "";
	@SuppressWarnings("serial")
	private transient ManyToManySet<RoomPlan, WallPlan> wallPlansWrapper = new ManyToManySet<RoomPlan, WallPlan>(this) {
		public Set<WallPlan> getDelegate() {
			return wallPlans;
		}

		@SuppressWarnings("unchecked")
		protected ManyToManyCollection<WallPlan, RoomPlan> getOtherEnd(WallPlan other) {
			return (ManyToManyCollection<WallPlan, RoomPlan>) other.getRoomPlans();
		}

		public boolean isLoaded() {
			return true;
		}

		public boolean isInstanceOfChild(Object o) {
			return o instanceof WallPlan;
		}
	};
	@Collection(jcrName = "test:wallPlans", collectionConverter = BeanReferenceCollectionConverterImpl.class)
	@ManyToMany()
	@JoinTable(name = "room_plan_wall_plan", joinColumns = { @JoinColumn(name = "wall_plans_id", referencedColumnName = "id") },
			inverseJoinColumns = { @JoinColumn(name = "room_plans_id", referencedColumnName = "id") })
	private Set<WallPlan> wallPlans = new HashSet<WallPlan>();
	@Field(path = true)
	String path;
	@Field(jcrName = "test:uuid", jcrType = "String")
	@javax.persistence.Basic()
	private String uuid = getUuid();

	public RoomPlan() {
	}

	public RoomPlan(HousePlan owner) {
		this.setHousePlan(owner);
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

	public Set<WallPlan> getWallPlans() {
		Set<WallPlan> result = this.wallPlansWrapper;
		return result;
	}

	public void setHousePlan(HousePlan newHousePlan) {
		if (!(newHousePlan == null)) {
			newHousePlan.getRoomPlans().add(this);
		} else {
			if (!(this.housePlan == null)) {
				this.housePlan.getRoomPlans().remove(this);
			}
		}
		this.housePlan = newHousePlan;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void setWallPlans(Set<WallPlan> newWallPlans) {
		this.wallPlans = newWallPlans;
	}

	public void zz_internalSetHousePlan(HousePlan value) {
		this.housePlan = value;
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
		return o instanceof RoomPlan && ((RoomPlan) o).getUuid().equals(getUuid());
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
