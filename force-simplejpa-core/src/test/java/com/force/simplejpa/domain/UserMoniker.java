/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author dbuccola
 */
public class UserMoniker {
    @Id
    @Column( name = "Id")
    private String id;

    @Column( name = "Name")
    private String name;

    public UserMoniker( String id ) {
        this.id = id;
    }

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
}
