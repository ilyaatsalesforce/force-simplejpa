/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * @author davidbuccola
 */
@Entity
public class SimpleContainerBean {
    private String id;

    @OneToMany
    @Column(name = "relatedBeans")
    private List<SimpleBean> relatedBeans;

    @OneToMany
    private SimpleBean[] moreRelatedBeans;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SimpleBean> getRelatedBeans() {
        return relatedBeans;
    }

    public void setRelatedBeans(List<SimpleBean> relatedBeans) {
        this.relatedBeans = relatedBeans;
    }

    public SimpleBean[] getMoreRelatedBeans() {
        return moreRelatedBeans;
    }

    public void setMoreRelatedBeans(SimpleBean[] moreRelatedBeans) {
        this.moreRelatedBeans = moreRelatedBeans;
    }
}
