/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import com.force.simplejpa.domain.InsertableUpdatableBean;
import com.force.simplejpa.domain.SimpleBean;
import com.force.simplejpa.domain.SimpleContainerBean;
import com.force.simplejpa.domain.StandardFieldBean;
import com.force.simplejpa.domain.UserMoniker;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author davidbuccola
 */
public class SimpleEntityManagerTest extends AbstractSimpleEntityManagerTest {
    @Test
    public void testPersistSuccess() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");
        bean.setState("This is transient");

        when(
            mockConnector.doCreate(anyString(), anyString()))
            .thenReturn(getResourceStream("persistSuccessResponse.json"));

        em.persist(bean);

        verify(mockConnector).doCreate("SimpleBean", getResourceString("persistSuccessRequest.json"));
    }

    @Test(expected = EntityResponseException.class)
    public void testPersistError() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");

        when(
            mockConnector.doCreate(anyString(), anyString()))
            .thenReturn(getResourceStream("persistErrorResponse.json"));

        em.persist(bean);
    }

    @Test(expected = EntityResponseException.class)
    public void testPersistInvalidResponse() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");

        when(
            mockConnector.doCreate(anyString(), anyString()))
            .thenReturn(getResourceStream("persistInvalidResponse.json"));

        em.persist(bean);
    }

    @Test
    public void testPersistStandardFields() throws Exception {
        StandardFieldBean bean = new StandardFieldBean();
        bean.setName("Name 1");
        bean.setCreatedBy(new UserMoniker("a01i00000000201"));
        bean.setCreatedDate(new Date());
        bean.setLastModifiedBy(new UserMoniker("a01i00000000202"));
        bean.setLastModifiedDate(new Date());
        bean.setOwner(new UserMoniker("a01i00000000203"));

        when(
            mockConnector.doCreate(anyString(), anyString()))
            .thenReturn(getResourceStream("persistSuccessResponse.json"));

        em.persist(bean);

        verify(mockConnector).doCreate("StandardFieldBean", getResourceString("persistStandardFieldsRequest.json"));
    }

    @Test
    public void testPersistInsertableUpdatable() throws Exception {
        InsertableUpdatableBean bean = new InsertableUpdatableBean();
        bean.setName("Name 1");
        bean.setNotInsertable("Updatable but not insertable value");
        bean.setNotUpdatable("Insertable but not updatable value");
        bean.setNotInsertableOrUpdatable("Not insertable or updatable value");

        when(
            mockConnector.doCreate(anyString(), anyString()))
            .thenReturn(getResourceStream("persistSuccessResponse.json"));

        em.persist(bean);

        verify(mockConnector).doCreate("InsertableUpdatableBean", getResourceString("persistInsertableUpdatableRequest.json"));
    }

    @Test
    public void testMergeSuccess() throws Exception {
        SimpleBean simpleBeanChanges = new SimpleBean();
        simpleBeanChanges.setId("a01i00000000001AAC");
        simpleBeanChanges.setDescription("Description 1");

        doNothing().when(mockConnector).doUpdate(anyString(), anyString(), anyString());
        em.merge(simpleBeanChanges);

        verify(mockConnector).doUpdate("SimpleBean", "a01i00000000001AAC", getResourceString("mergeSuccessRequest.json"));
    }

    @Test(expected = EntityRequestException.class)
    public void testMergeNoId() throws Exception {
        SimpleBean simpleBeanChanges = new SimpleBean();
        simpleBeanChanges.setDescription("Description 1");

        doNothing().when(mockConnector).doUpdate(anyString(), anyString(), anyString());
        em.merge(simpleBeanChanges);
    }

    @Test
    public void testMergeStandardFields() throws Exception {
        StandardFieldBean standardFieldBeanChanges = new StandardFieldBean();
        standardFieldBeanChanges.setId("a01i00000000001AAC");
        standardFieldBeanChanges.setName("Name 1");
        standardFieldBeanChanges.setCreatedBy(new UserMoniker("a01i00000000201"));
        standardFieldBeanChanges.setCreatedDate(new Date());
        standardFieldBeanChanges.setLastModifiedBy(new UserMoniker("a01i00000000202"));
        standardFieldBeanChanges.setLastModifiedDate(new Date());
        standardFieldBeanChanges.setOwner(new UserMoniker("a01i00000000203"));

        doNothing().when(mockConnector).doUpdate(anyString(), anyString(), anyString());
        em.merge(standardFieldBeanChanges);

        verify(mockConnector).doUpdate("StandardFieldBean", "a01i00000000001AAC", getResourceString("mergeStandardFieldsRequest.json"));
    }

    @Test
    public void testRemoveSuccess() throws Exception {
        SimpleBean simpleBean = new SimpleBean();
        simpleBean.setId("a01i00000000001AAC");

        doNothing().when(mockConnector).doDelete(anyString(), anyString());
        em.remove(simpleBean);

        verify(mockConnector).doDelete("SimpleBean", "a01i00000000001AAC");
    }

    @Test(expected = EntityRequestException.class)
    public void testRemoveNoId() throws Exception {
        SimpleBean simpleBean = new SimpleBean();
        simpleBean.setDescription("Description 1");

        doNothing().when(mockConnector).doDelete(anyString(), anyString());
        em.remove(simpleBean);
    }

    @Test
    public void testFindSuccess() throws Exception {
        when(mockConnector.doQuery(anyString())).thenReturn(getResourceStream("findSuccessResponse.json"));

        SimpleBean simpleBean = em.find(SimpleBean.class, "a01i00000000001AAC");

        assertNotNull(simpleBean);

        assertEquals("SimpleBean", simpleBean.getAttributes().get("type"));
        assertEquals("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001", simpleBean.getAttributes().get("url"));
        assertEquals("a01i00000000001", simpleBean.getId());
        assertEquals("Name 1", simpleBean.getName());
        assertEquals("Description 1", simpleBean.getDescription());
    }

    @Test
    public void testSimpleQueryResult() throws Exception {
        when(mockConnector.doQuery(anyString())).thenReturn(getResourceStream("simpleQueryResponse.json"));

        List<SimpleBean> beans = em.createQuery("select * from SimpleBean", SimpleBean.class).getResultList();

        assertEquals(2, beans.size());

        SimpleBean bean1 = beans.get(0);
        assertEquals("SimpleBean", bean1.getAttributes().get("type"));
        assertEquals("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001", bean1.getAttributes().get("url"));
        assertEquals("a01i00000000001", bean1.getId());
        assertEquals("Name 1", bean1.getName());
        assertEquals("Description 1", bean1.getDescription());

        SimpleBean bean2 = beans.get(1);
        assertEquals("SimpleBean", bean2.getAttributes().get("type"));
        assertEquals("/services/data/v28.0/sobjects/SimpleBean/a01i00000000002", bean2.getAttributes().get("url"));
        assertEquals("a01i00000000002", bean2.getId());
        assertEquals("Name 2", bean2.getName());
        assertEquals("Description 2", bean2.getDescription());
    }

    @Test
    public void testSimpleSubqueryResult() throws Exception {
        when(mockConnector.doQuery(anyString())).thenReturn(getResourceStream("simpleSubqueryResponse.json"));

        List<SimpleContainerBean> containerBeans =
            em.createQuery("select * from SimpleContainerBean", SimpleContainerBean.class).getResultList();

        assertEquals(1, containerBeans.size());

        SimpleContainerBean containerBean1 = containerBeans.get(0);
        assertEquals(2, containerBean1.getRelatedBeans().size());

        SimpleBean bean1 = containerBean1.getRelatedBeans().get(0);
        assertEquals("SimpleBean", bean1.getAttributes().get("type"));
        assertEquals("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001", bean1.getAttributes().get("url"));
        assertEquals("a01i00000000001", bean1.getId());
        assertEquals("Name 1", bean1.getName());
        assertEquals("Description 1", bean1.getDescription());

        SimpleBean bean2 = containerBean1.getRelatedBeans().get(1);
        assertEquals("SimpleBean", bean2.getAttributes().get("type"));
        assertEquals("/services/data/v28.0/sobjects/SimpleBean/a01i00000000002", bean2.getAttributes().get("url"));
        assertEquals("a01i00000000002", bean2.getId());
        assertEquals("Name 2", bean2.getName());
        assertEquals("Description 2", bean2.getDescription());

        assertEquals(2, containerBean1.getMoreRelatedBeans().length);

        SimpleBean bean3 = containerBean1.getMoreRelatedBeans()[0];
        assertEquals("SimpleBean", bean3.getAttributes().get("type"));
        assertEquals("/services/data/v28.0/sobjects/SimpleBean/a01i00000000003", bean3.getAttributes().get("url"));
        assertEquals("a01i00000000003", bean3.getId());
        assertEquals("Name 3", bean3.getName());
        assertEquals("Description 3", bean3.getDescription());

        SimpleBean bean4 = containerBean1.getMoreRelatedBeans()[1];
        assertEquals("SimpleBean", bean4.getAttributes().get("type"));
        assertEquals("/services/data/v28.0/sobjects/SimpleBean/a01i00000000004", bean4.getAttributes().get("url"));
        assertEquals("a01i00000000004", bean4.getId());
        assertEquals("Name 4", bean4.getName());
        assertEquals("Description 4", bean4.getDescription());
    }
}
