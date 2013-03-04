/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A test bean with fields that choose whether they are insertable or updatable.
 *
 * @author dbuccola
 */
@Entity
public class InsertableUpdatableBean {
    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "Name")
    private String name;

    @Column(name = "NotInsertable", insertable = false)
    private String notInsertable;

    @Column(name = "NotUpdatable", updatable = false)
    private String notUpdatable;

    @Column(name = "NotInsertableOrUpdatable", insertable = false, updatable = false)
    private String notInsertableOrUpdatable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotInsertable() {
        return notInsertable;
    }

    public void setNotInsertable(String notInsertable) {
        this.notInsertable = notInsertable;
    }

    public String getNotInsertableOrUpdatable() {
        return notInsertableOrUpdatable;
    }

    public void setNotInsertableOrUpdatable(String notInsertableOrUpdatable) {
        this.notInsertableOrUpdatable = notInsertableOrUpdatable;
    }

    public String getNotUpdatable() {
        return notUpdatable;
    }

    public void setNotUpdatable(String notUpdatable) {
        this.notUpdatable = notUpdatable;
    }
}
