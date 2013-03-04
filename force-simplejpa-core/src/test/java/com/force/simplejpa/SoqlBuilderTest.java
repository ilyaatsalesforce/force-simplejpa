/*
 * Copyright, 1999-2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import com.force.simplejpa.domain.RecursiveBean;
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
    public void testBasicWildcard() throws Exception {
        String soqlTemplate = "select * from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void testWildcardWithOnePartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Id,Prefix1.Name,Prefix1.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void testWildcardWithTwoPartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.Prefix2.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Prefix2.Id,Prefix1.Prefix2.Name,Prefix1.Prefix2.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void testWildcardWithEmptyEntityNameQualifier() throws Exception {
        String soqlTemplate = "select *{} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void testWildcardWithEntityNameQualifier() throws Exception {
        String soqlTemplate = "select *{SimpleBean} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardWithWrongEntityNameQualifier() throws Exception {
        String soqlTemplate = "select *{UnknownBean} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void testRelationshipSubquery() throws Exception {
        String soqlTemplate = "select (select RelatedBean.* from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select (select RelatedBean.Id,RelatedBean.Name,RelatedBean.Description from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }

    @Test
    public void testRecursiveTypeReferences() throws Exception {
        String soqlTemplate = "select * from RecursiveBean where Id = '012345678901234'";
        String expectedSoql = "select Id,RecursiveBean.Id,RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id from RecursiveBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(descriptorProvider.get(RecursiveBean.class)).soqlTemplate(soqlTemplate).build();
        Assert.assertEquals(expectedSoql, soql);
    }
}
