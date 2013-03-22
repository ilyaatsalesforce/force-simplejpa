/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

@Entity
public class NoGetterBean {
    @Id
    @Column(name = "Id")
    public String id;

    @Column(name = "Value1")
    private String value1;

    private Map<String, String> attributes;

    public void setId(String id) {
        this.id = id;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
