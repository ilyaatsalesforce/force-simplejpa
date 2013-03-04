/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * A test bean that includes the standard fields.
 *
 * @author dbuccola
 */
@Entity
public class StandardFieldBean {
    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "Name")
    private String name;

    @ManyToOne
    @Column(name = "CreatedBy")
    private UserMoniker createdBy;

    @Column(name = "CreatedDate")
    private Date createdDate;

    @ManyToOne
    @Column(name = "LastModifiedBy")
    private UserMoniker lastModifiedBy;

    @Column(name = "LastModifiedDate")
    private Date lastModifiedDate;

    @ManyToOne
    @Column(name = "Owner")
    private UserMoniker owner;

    public UserMoniker getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserMoniker createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserMoniker getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(UserMoniker lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserMoniker getOwner() {
        return owner;
    }

    public void setOwner(UserMoniker owner) {
        this.owner = owner;
    }
}
