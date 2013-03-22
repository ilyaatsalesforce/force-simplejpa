/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class SimpleContainerBean {
    @Id
    @Column(name = "Id")
    private String id;

    @OneToMany
    @Column(name = "RelatedBeans")
    private List<SimpleBean> relatedBeans;

    @OneToMany
    @Column(name = "MoreRelatedBeans")
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
