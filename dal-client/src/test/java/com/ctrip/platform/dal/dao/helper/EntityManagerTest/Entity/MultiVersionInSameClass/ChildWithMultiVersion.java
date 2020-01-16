package com.ctrip.platform.dal.dao.helper.EntityManagerTest.Entity.MultiVersionInSameClass;

import com.ctrip.platform.dal.dao.annotation.Database;
import com.ctrip.platform.dal.dao.annotation.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import java.sql.Types;

@Entity
@Database(name = "ChildDatabase")
@Table(name = "ChildTable")
public class ChildWithMultiVersion {
    @Column(name = "childName")
    @Type(value = Types.VARCHAR)
    @Version
    private String childName;

    @Column(name = "childName2")
    @Type(value = Types.VARCHAR)
    @Version
    private String childName2;

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getChildName2() {
        return childName2;
    }

    public void setChildName2(String childName2) {
        this.childName2 = childName2;
    }

}
