package test;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.common.ocm.GrandParentBeanConverterImpl;
@Node(jcrType = "test:wall", discriminator = false)
@Entity(name="Wall")
@Table(name="wall")
public class Wall{
  @Bean(jcrName = "test:house", converter=GrandParentBeanConverterImpl.class)
  @ManyToOne()
  @JoinColumns(value={
        @JoinColumn(name="house_id",referencedColumnName="id")}
  )
  private House house = null;
  @Field(uuid = true)
  @Id()
  @GeneratedValue()
  private String id = null;
  @Field(path=true)
  String path;
  public Wall(){
  }
  public Wall(House owner){
  this.setHouse(owner);
  }
  public House getHouse(){
    House result = this.house;
    return result;
  }
  public String getId(){
    String result = this.id;
    return result;
  }
  public void setHouse(House newHouse){
    if(!(newHouse == null)){
      newHouse.getWalls().add(this);
    } else {
      if(!(this.house == null)){
        this.house.getWalls().remove(this);
      }
    }
    this.house=newHouse;
  }
  public void setId(String id){
    this.id=id;
  }
  public void zz_internalSetHouse(House value){
    this.house=value;
  }
  public String getPath(){
    return this.path;
  }
  public void setPath(String value){
    this.path=value;
  }
}
