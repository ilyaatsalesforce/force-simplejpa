/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import com.force.simplejpa.domain.*;
import org.codehaus.jackson.JsonNode;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.*;

public class SimpleEntityManagerTest extends AbstractSimpleEntityManagerTest {

    @Test
    public void testPersistSuccess() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");
        bean.setState("This is transient");

        when(
            mockConnector.doCreate(anyString(), anyString(), anyMapOf(String.class, String.class)))
            .thenReturn(getResourceStream("persistSuccessResponse.json"));

        em.persist(bean);

        verify(mockConnector).doCreate("SimpleBean", getResourceString("persistSuccessRequest.json"), null);
    }

    @Test
    public void testPersistWithIdSet() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setId("012345678901234");

        try {
            em.persist(bean);
            fail("Didn't get expected exception");
        } catch (EntityRequestException e) {
            assertThat(e.getMessage(), is(equalTo("Id value should not exist for new object creation")));
        }
    }

    @Test
    public void testPersistErrorResponse() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");

        when(
            mockConnector.doCreate(anyString(), anyString(), anyMapOf(String.class, String.class)))
            .thenReturn(getResourceStream("persistErrorResponse.json"));

        try {
            em.persist(bean);
            fail("Didn't get expected exception");
        } catch (EntityResponseException e) {
            assertThat(e.getMessage(), is(equalTo("Error message 1; Error message 2")));
        }
    }

    @Test
    public void testPersistInvalidResponse() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("Name 1");
        bean.setDescription("Description 1");

        when(
            mockConnector.doCreate(anyString(), anyString(), anyMapOf(String.class, String.class)))
            .thenReturn(getResourceStream("persistInvalidResponse.json"));

        try {
            em.persist(bean);
            fail("Didn't get expected exception");
        } catch (EntityResponseException e) {
            assertThat(e.getMessage(), is(equalTo("JSON response is missing expected fields")));
        }
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
            mockConnector.doCreate(anyString(), anyString(), anyMapOf(String.class, String.class)))
            .thenReturn(getResourceStream("persistSuccessResponse.json"));

        em.persist(bean);

        verify(mockConnector).doCreate("StandardFieldBean", getResourceString("persistStandardFieldsRequest.json"), null);
    }

    @Test
    public void testPersistInsertableUpdatable() throws Exception {
        InsertableUpdatableBean bean = new InsertableUpdatableBean();
        bean.setName("Name 1");
        bean.setNotInsertable("Updatable but not insertable value");
        bean.setNotUpdatable("Insertable but not updatable value");
        bean.setNotInsertableOrUpdatable("Not insertable or updatable value");

        when(
            mockConnector.doCreate(anyString(), anyString(), anyMapOf(String.class, String.class)))
            .thenReturn(getResourceStream("persistSuccessResponse.json"));

        em.persist(bean);

        verify(mockConnector).doCreate("InsertableUpdatableBean", getResourceString("persistInsertableUpdatableRequest.json"), null);
    }

    @Test
    public void testMergeSuccess() throws Exception {
        SimpleBean simpleBeanChanges = new SimpleBean();
        simpleBeanChanges.setId("a01i00000000001AAC");
        simpleBeanChanges.setDescription("Description 1");

        doNothing().when(mockConnector).doUpdate(anyString(), anyString(), anyString(), anyMapOf(String.class, String.class));
        em.merge(simpleBeanChanges);

        verify(mockConnector).doUpdate("SimpleBean", "a01i00000000001AAC", getResourceString("mergeSuccessRequest.json"), null);
    }

    @Test(expected = EntityRequestException.class)
    public void testMergeNoId() throws Exception {
        SimpleBean simpleBeanChanges = new SimpleBean();
        simpleBeanChanges.setDescription("Description 1");

        doNothing().when(mockConnector).doUpdate(anyString(), anyString(), anyString(), anyMapOf(String.class, String.class));
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

        doNothing().when(mockConnector).doUpdate(anyString(), anyString(), anyString(), anyMapOf(String.class, String.class));
        em.merge(standardFieldBeanChanges);

        verify(mockConnector).doUpdate("StandardFieldBean", "a01i00000000001AAC", getResourceString("mergeStandardFieldsRequest.json"), null);
    }

    @Test
    public void testRemoveSuccess() throws Exception {
        SimpleBean simpleBean = new SimpleBean();
        simpleBean.setId("a01i00000000001AAC");

        doNothing().when(mockConnector).doDelete(anyString(), anyString(), anyMapOf(String.class, String.class));
        em.remove(simpleBean);

        verify(mockConnector).doDelete("SimpleBean", "a01i00000000001AAC", null);
    }

    @Test(expected = EntityRequestException.class)
    public void testRemoveNoId() throws Exception {
        SimpleBean simpleBean = new SimpleBean();
        simpleBean.setDescription("Description 1");

        doNothing().when(mockConnector).doDelete(anyString(), anyString(), anyMapOf(String.class, String.class));
        em.remove(simpleBean);
    }

    @Test
    public void testFindSuccess() throws Exception {
        when(mockConnector.doQuery(anyString(), anyMapOf(String.class, String.class))).thenReturn(getResourceStream("findSuccessResponse.json"));

        SimpleBean bean1 = em.find(SimpleBean.class, "a01i00000000001AAC");

        assertThat(bean1, is(not(nullValue())));

        assertThat(bean1.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean1.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001")));
        assertThat(bean1.getId(), is(equalTo("a01i00000000001")));
        assertThat(bean1.getName(), is(equalTo("Name 1")));
        assertThat(bean1.getDescription(), is(equalTo("Description 1")));
    }

    @Test
    public void testSimpleQuery() throws Exception {
        when(mockConnector.doQuery(anyString(), anyMapOf(String.class, String.class))).thenReturn(getResourceStream("simpleQueryResponse.json"));

        List<SimpleBean> beans = em.createQuery("select * from SimpleBean", SimpleBean.class).getResultList();

        assertThat(beans.size(), is(equalTo(2)));

        SimpleBean bean1 = beans.get(0);
        assertThat(bean1.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean1.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001")));
        assertThat(bean1.getId(), is(equalTo("a01i00000000001")));
        assertThat(bean1.getName(), is(equalTo("Name 1")));
        assertThat(bean1.getDescription(), is(equalTo("Description 1")));

        SimpleBean bean2 = beans.get(1);
        assertThat(bean2.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean2.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000002")));
        assertThat(bean2.getId(), is(equalTo("a01i00000000002")));
        assertThat(bean2.getName(), is(equalTo("Name 2")));
        assertThat(bean2.getDescription(), is(equalTo("Description 2")));
    }

    @Test
    public void testSubquery() throws Exception {
        when(mockConnector.doQuery(anyString(), anyMapOf(String.class, String.class))).thenReturn(getResourceStream("simpleSubqueryResponse.json"));

        List<SimpleContainerBean> containerBeans =
            em.createQuery("select * from SimpleContainerBean", SimpleContainerBean.class).getResultList();

        assertThat(containerBeans.size(), is(equalTo(1)));

        SimpleContainerBean containerBean1 = containerBeans.get(0);
        assertThat(containerBean1.getRelatedBeans().size(), is(equalTo(2)));

        SimpleBean bean1 = containerBean1.getRelatedBeans().get(0);
        assertThat(bean1.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean1.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000001")));
        assertThat(bean1.getId(), is(equalTo("a01i00000000001")));
        assertThat(bean1.getName(), is(equalTo("Name 1")));
        assertThat(bean1.getDescription(), is(equalTo("Description 1")));

        SimpleBean bean2 = containerBean1.getRelatedBeans().get(1);
        assertThat(bean2.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean2.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000002")));
        assertThat(bean2.getId(), is(equalTo("a01i00000000002")));
        assertThat(bean2.getName(), is(equalTo("Name 2")));
        assertThat(bean2.getDescription(), is(equalTo("Description 2")));

        assertThat(containerBean1.getMoreRelatedBeans().length, is(equalTo(2)));

        SimpleBean bean3 = containerBean1.getMoreRelatedBeans()[0];
        assertThat(bean3.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean3.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000003")));
        assertThat(bean3.getId(), is(equalTo("a01i00000000003")));
        assertThat(bean3.getName(), is(equalTo("Name 3")));
        assertThat(bean3.getDescription(), is(equalTo("Description 3")));

        SimpleBean bean4 = containerBean1.getMoreRelatedBeans()[1];
        assertThat(bean4.getAttributes().get("type"), is(equalTo("SimpleBean")));
        assertThat(bean4.getAttributes().get("url"), is(equalTo("/services/data/v28.0/sobjects/SimpleBean/a01i00000000004")));
        assertThat(bean4.getId(), is(equalTo("a01i00000000004")));
        assertThat(bean4.getName(), is(equalTo("Name 4")));
        assertThat(bean4.getDescription(), is(equalTo("Description 4")));
    }

    @Test
    public void testAggregateQuery() throws Exception {
        when(mockConnector.doQuery(anyString(), anyMapOf(String.class, String.class))).thenReturn(getResourceStream("aggregateQueryResponse.json"));

        List<JsonNode> jsonNodes =
            em.createQuery("select count(Id),Name FROM SimpleBean GROUP BY Name", SimpleBean.class).getResultList(JsonNode.class);

        assertThat(jsonNodes.size(), is(equalTo(2)));

        JsonNode node1 = jsonNodes.get(0);
        assertThat(node1.get("expr0").asInt(), is(equalTo(1)));
        assertThat(node1.get("Name").asText(), is(equalTo("Name 1")));

        JsonNode node2 = jsonNodes.get(1);
        assertThat(node2.get("expr0").asInt(), is(equalTo(1)));
        assertThat(node2.get("Name").asText(), is(equalTo("Name 2")));
    }
}
