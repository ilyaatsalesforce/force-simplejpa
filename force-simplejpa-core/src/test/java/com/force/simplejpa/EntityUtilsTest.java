/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import com.force.simplejpa.domain.NoGetterBean;
import com.force.simplejpa.domain.NoSetterBean;
import com.force.simplejpa.domain.SimpleBean;
import com.force.simplejpa.domain.UnannotatedBean;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class EntityUtilsTest {
    private EntityMappingContext mappingContext = new EntityMappingContext();

    @Test
    public void testGetAttributesSimple() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(SimpleBean.class);
        SimpleBean instance = new SimpleBean();
        Map<String, String> attributes = new HashMap<String, String>();
        instance.setAttributes(attributes);

        assertThat(EntityUtils.getAttributes(descriptor, instance), is(sameInstance(attributes)));
        assertThat(EntityUtils.getAttributes(descriptor.getAttributesProperty(), instance), is(sameInstance(attributes)));
    }

    @Test
    public void testGetAttributesNoSetter() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();

        assertThat(EntityUtils.getAttributes(descriptor, instance), is(nullValue()));
        assertThat(EntityUtils.getAttributes(descriptor.getAttributesProperty(), instance), is(nullValue()));
    }

    @Test
    public void testGetAttributesNoGetter() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        Map<String, String> attributes = new HashMap<String, String>();
        instance.setAttributes(attributes);

        assertThat(EntityUtils.getAttributes(descriptor, instance), is(sameInstance(attributes)));
        assertThat(EntityUtils.getAttributes(descriptor.getAttributesProperty(), instance), is(sameInstance(attributes)));
    }

    @Test
    public void testGetAttributesWhenNone() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(UnannotatedBean.class);
        UnannotatedBean instance = new UnannotatedBean();

        try {
            EntityUtils.getAttributes(descriptor, instance);
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            // Exception expected because no attribute property exists.
        }
    }

    @Test
    public void testGetIdSimple() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(SimpleBean.class);
        SimpleBean instance = new SimpleBean();
        String id = "012345678901234";
        instance.setId(id);

        assertThat(EntityUtils.getEntityId(descriptor, instance), is(sameInstance(id)));
        assertThat(EntityUtils.getEntityId(descriptor.getIdProperty(), instance), is(sameInstance(id)));
    }

    @Test
    public void testGetIdNoSetter() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();

        assertThat(EntityUtils.getEntityId(descriptor, instance), is(nullValue()));
        assertThat(EntityUtils.getEntityId(descriptor.getIdProperty(), instance), is(nullValue()));
    }

    @Test
    public void testGetIdNoGetter() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        String id = "012345678901234";
        instance.setId(id);

        assertThat(EntityUtils.getEntityId(descriptor, instance), is(sameInstance(id)));
        assertThat(EntityUtils.getEntityId(descriptor.getIdProperty(), instance), is(sameInstance(id)));
    }

    @Test
    public void testGetIdWhenNone() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(UnannotatedBean.class);
        UnannotatedBean instance = new UnannotatedBean();

        try {
            EntityUtils.getEntityId(descriptor, instance);
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            // Exception expected because no id property exists.
        }
    }

    @Test
    public void testSetIdSimple() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(SimpleBean.class);
        SimpleBean instance = new SimpleBean();
        String id = "012345678901234";

        EntityUtils.setEntityId(descriptor, instance, id);
        assertThat(instance.getId(), is(sameInstance(id)));
    }

    @Test
    public void testSetIdNoSetter() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoSetterBean.class);
        NoSetterBean instance = new NoSetterBean();
        String id = "012345678901234";

        EntityUtils.setEntityId(descriptor, instance, id);
        assertThat(instance.getId(), is(sameInstance(id)));
    }

    @Test
    public void testSetIdNoGetter() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(NoGetterBean.class);
        NoGetterBean instance = new NoGetterBean();
        String id = "012345678901234";

        EntityUtils.setEntityId(descriptor, instance, id);
        assertThat(instance.id, is(sameInstance(id)));
    }

    @Test
    public void testSetIdWhenNone() {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(UnannotatedBean.class);
        UnannotatedBean instance = new UnannotatedBean();
        String id = "012345678901234";

        try {
            EntityUtils.setEntityId(descriptor, instance, id);
            fail("Didn't get expected exception");
        } catch (IllegalArgumentException e) {
            // Exception expected because no id property exists.
        }
    }
}
