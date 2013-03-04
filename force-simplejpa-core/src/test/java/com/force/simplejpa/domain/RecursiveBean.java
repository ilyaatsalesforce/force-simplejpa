/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * A test bean that is helps test recursive type references.
 *
 * @author dbuccola
 */
public class RecursiveBean {
    @Id
    @Column(name = "Id")
    private String id;

    @ManyToOne
    @Column(name = "RecursiveBean")
    private RecursiveBean recursiveBean;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RecursiveBean getRecursiveBean() {
        return recursiveBean;
    }

    public void setRecursiveBean(RecursiveBean recursiveBean) {
        this.recursiveBean = recursiveBean;
    }
}
