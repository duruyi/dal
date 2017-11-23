package shardTest.oldVersionCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.ctrip.platform.dal.dao.annotation.Database;
import com.ctrip.platform.dal.dao.annotation.Type;
import java.sql.Types;
import java.sql.Timestamp;
import com.ctrip.platform.dal.dao.DalPojo;

@Entity
@Database(name="SimpleShardByTableOnMySql")
@Table(name="person")
public class SimpleShardByTableOnMysqlGen implements DalPojo {
	
	@Id
	@Column(name="ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Type(value=Types.INTEGER)
	private Integer iD;
	
	@Column(name="Name")
	@Type(value=Types.VARCHAR)
	private String name;
	
	@Column(name="Age")
	@Type(value=Types.INTEGER)
	private Integer age;
	
	@Column(name="Birth")
	@Type(value=Types.TIMESTAMP)
	private Timestamp birth;

	public Integer getID() {
		return iD;
	}

	public void setID(Integer iD) {
		this.iD = iD;
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

	public Timestamp getBirth() {
		return birth;
	}

	public void setBirth(Timestamp birth) {
		this.birth = birth;
	}

}