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
public class NoSetterBean {
    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "Value1")
    private String value1;

    private Map<String, String> attributes;

    public String getId() {
        return id;
    }

    public String getValue1() {
        return value1;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
