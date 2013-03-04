/*
 * Copyright, 1999-2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import com.force.simplejpa.domain.SimpleBean;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author davidbuccola
 */
public class SoqlBuilderTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final EntityDescriptorProvider descriptorProvider = new EntityDescriptorProvider(objectMapper);

    static {
        objectMapper.setSerializationConfig(
                objectMapper.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .withPropertyNamingStrategy(new EntityPropertyNamingStrategy(true))
                .withAnnotationIntrospector(new SimpleJpaAnnotationIntrospector(descriptorProvider)));
        objectMapper.setDeserializationConfig(
                objectMapper.getDeserializationConfig()
                .withPropertyNamingStrategy(new EntityPropertyNamingStrategy(false))
                .withAnnotationIntrospector(new SimpleJpaAnnotationIntrospector(descriptorProvider)));
    }

    @Test
    public void noPrefix() throws Exception {
        String soqlTemplate = "select * from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void onePartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Id,Prefix1.Name,Prefix1.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void twoPartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.Prefix2.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Prefix2.Id,Prefix1.Prefix2.Name,Prefix1.Prefix2.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void emptyEntityName() throws Exception {
        String soqlTemplate = "select *{} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void rightEntityName() throws Exception {
        String soqlTemplate = "select *{SimpleBean} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongEntityName() throws Exception {
        String soqlTemplate = "select *{UnknownBean} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void withRelationshipQuery() throws Exception {
        String soqlTemplate = "select (select RelatedBean.* from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select (select RelatedBean.Id,RelatedBean.Name,RelatedBean.Description from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }
}
