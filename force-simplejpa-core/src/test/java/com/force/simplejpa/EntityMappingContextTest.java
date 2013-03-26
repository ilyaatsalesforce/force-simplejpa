/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import com.force.simplejpa.domain.*;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Test;

import static com.force.simplejpa.HasPropertyName.hasPropertyName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("unchecked")
public class EntityMappingContextTest {
    private EntityMappingContext mappingContext = new EntityMappingContext();

    @Test
    public void testSimpleBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(SimpleBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("SimpleBean")));
        assertThat(descriptor.hasAttributesMember(), is(true));
        assertThat(descriptor.getAttributesProperty(), is(not(nullValue())));
        assertThat(descriptor.getAttributesProperty(), hasPropertyName("attributes"));
        assertThat(descriptor.hasIdMember(), is(true));
        assertThat(descriptor.getIdProperty(), is(not(nullValue())));
        assertThat(descriptor.getIdProperty(), hasPropertyName("Id"));
        assertThat(descriptor.getRelatedEntities().size(), is(equalTo(0)));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Name"),
                hasPropertyName("Description"),
                hasPropertyName("attributes")));
    }

    @Test
    public void testSimpleContainerBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(SimpleContainerBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("SimpleContainerBean")));
        assertThat(descriptor.getAttributesProperty(), is(nullValue()));
        assertThat(descriptor.getIdProperty(), is(not(nullValue())));
        assertThat(descriptor.getIdProperty(), hasPropertyName("Id"));
        assertThat(descriptor.getRelatedEntities().size(), is(equalTo(2)));
        assertThat(descriptor.getRelatedEntities(), hasKey("relatedBeans"));
        assertThat(descriptor.getRelatedEntities(), hasKey("moreRelatedBeans"));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("RelatedBeans"),
                hasPropertyName("MoreRelatedBeans")));
    }

    @Test
    public void testCustomBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(CustomBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("namespace__CustomBean__c")));
        assertThat(descriptor.getIdProperty(), is(not(nullValue())));
        assertThat(descriptor.getIdProperty(), hasPropertyName("Id"));
        assertThat(descriptor.getRelatedEntities().size(), is(equalTo(1)));
        assertThat(descriptor.getRelatedEntities(), hasKey("relatedBeans"));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Name"),
                hasPropertyName("namespace__Value1__c"),
                hasPropertyName("namespace__Value2__c"),
                hasPropertyName("namespace__Value3__c"),
                hasPropertyName("namespace__RelatedBeans__c")));
    }

    @Test
    public void testNoSetterBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoSetterBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("NoSetterBean")));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Value1"),
                hasPropertyName("attributes")));
    }

    @Test
    public void testNoGetterBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoGetterBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("NoGetterBean")));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(
            descriptor.getBeanDescription().findProperties(),
            containsInAnyOrder(
                hasPropertyName("Id"),
                hasPropertyName("Value1"),
                hasPropertyName("attributes")));
    }

    @Test
    public void testExplicitlyNamedBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(ExplicitlyNamedBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("ExplicitName")));
    }

    @Test
    public void testUnannotatedBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(UnannotatedBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("UnannotatedBean")));
    }

    @Test
    public void testRecursiveBean() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(RecursiveBean.class);
        assertThat(descriptor, is(not(nullValue())));
        assertThat(descriptor.getName(), is(equalTo("RecursiveBean")));
        assertThat(descriptor.getBeanDescription(), is(not(nullValue())));
        assertThat(descriptor.getRelatedEntities().size(), is(equalTo(1)));
        assertThat(descriptor.getRelatedEntities().get("recursiveBean"), is(sameInstance(descriptor)));
    }

    @Test
    public void testDescriptorCaching() {
        EntityDescriptor descriptor1 = mappingContext.getEntityDescriptor(SimpleBean.class);
        EntityDescriptor descriptor2 = mappingContext.getEntityDescriptor(SimpleBean.class);
        assertThat(descriptor2, is(sameInstance(descriptor1)));
    }

    @Test
    public void testNoDescriptorForEnums() {
        assertThat(mappingContext.getEntityDescriptor(SimpleEnum.class), is(nullValue()));
    }

    @Test
    public void testNoDescriptorForPrimitives() {
        assertThat(mappingContext.getEntityDescriptor(int.class), is(nullValue()));
        assertThat(mappingContext.getEntityDescriptor(long.class), is(nullValue()));
        assertThat(mappingContext.getEntityDescriptor(float.class), is(nullValue()));
        assertThat(mappingContext.getEntityDescriptor(double.class), is(nullValue()));
        assertThat(mappingContext.getEntityDescriptor(boolean.class), is(nullValue()));
    }

    @Test
    public void testNoDescriptorForIntrinsicJavaPackage() {
        assertThat(mappingContext.getEntityDescriptor(Object.class), is(nullValue()));
        assertThat(mappingContext.getEntityDescriptor(String.class), is(nullValue()));
    }

    @Test
    public void testWriteDatesAsTimestampsDisabledByDefault() {
        assertThat(mappingContext.getObjectMapper().isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS),
            is(false));
    }
}
