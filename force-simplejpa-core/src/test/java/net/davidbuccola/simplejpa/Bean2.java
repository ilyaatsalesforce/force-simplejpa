/*
 * Copyright, 1999-2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * @author davidbuccola
 */
@Entity
public class Bean2 {
    private String id;

    @OneToMany
    @Column(name = "relatedBeans")
    private List<Bean1> relatedBeans;

    @OneToMany
    private Bean1[] moreRelatedBeans;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Bean1> getRelatedBeans() {
        return relatedBeans;
    }

    public void setRelatedBeans(List<Bean1> relatedBeans) {
        this.relatedBeans = relatedBeans;
    }

    public Bean1[] getMoreRelatedBeans() {
        return moreRelatedBeans;
    }

    public void setMoreRelatedBeans(Bean1[] moreRelatedBeans) {
        this.moreRelatedBeans = moreRelatedBeans;
    }
}
