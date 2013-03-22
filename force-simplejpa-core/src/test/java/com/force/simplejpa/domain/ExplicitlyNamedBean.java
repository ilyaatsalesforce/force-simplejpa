/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "ExplicitName")
public class ExplicitlyNamedBean {
    private String id;

    private String name;

    public String getId() {
        return id;
    }

    @Id
    @Column(name = "Id")
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Column(name = "Name")
    public void setName(String name) {
        this.name = name;
    }
}
