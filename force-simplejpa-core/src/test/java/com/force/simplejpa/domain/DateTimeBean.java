/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.domain;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class DateTimeBean {
    @Id
    @Column(name = "Id")
    private String id;

    @Column(name = "JavaDateAndTime")
    private Date javaDateAndTime;

    @Column(name = "JavaDateOnly")
    private Date javaDateOnly;

    @Column(name = "JodaDateAndTime")
    private DateTime jodaDateAndTime;

    @Column(name = "JodaDateOnly")
    private LocalDate jodaDateOnly;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getJavaDateAndTime() {
        return javaDateAndTime;
    }

    public void setJavaDateAndTime(Date javaDateAndTime) {
        this.javaDateAndTime = javaDateAndTime;
    }

    public Date getJavaDateOnly() {
        return javaDateOnly;
    }

    public void setJavaDateOnly(Date javaDateOnly) {
        this.javaDateOnly = javaDateOnly;
    }

    public DateTime getJodaDateAndTime() {
        return jodaDateAndTime;
    }

    public void setJodaDateAndTime(DateTime jodaDateAndTime) {
        this.jodaDateAndTime = jodaDateAndTime;
    }

    public LocalDate getJodaDateOnly() {
        return jodaDateOnly;
    }

    public void setJodaDateOnly(LocalDate jodaDateOnly) {
        this.jodaDateOnly = jodaDateOnly;
    }
}
