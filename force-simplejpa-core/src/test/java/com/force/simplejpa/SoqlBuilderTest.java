/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import com.force.simplejpa.domain.RecursiveBean;
import com.force.simplejpa.domain.SimpleBean;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SoqlBuilderTest {

    private static final EntityMappingContext mappingContext = new EntityMappingContext();

    @Test
    public void testBasicWildcard() throws Exception {
        String soqlTemplate = "select * from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithOnePartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Id,Prefix1.Name,Prefix1.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithTwoPartPrefix() throws Exception {
        String soqlTemplate = "select Prefix1.Prefix2.* from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Prefix1.Prefix2.Id,Prefix1.Prefix2.Name,Prefix1.Prefix2.Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithEmptyEntityNameQualifier() throws Exception {
        String soqlTemplate = "select *{} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testWildcardWithEntityNameQualifier() throws Exception {
        String soqlTemplate = "select *{SimpleBean} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardWithWrongEntityNameQualifier() throws Exception {
        String soqlTemplate = "select *{UnknownBean} from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select Id,Name,Description from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testRelationshipSubquery() throws Exception {
        String soqlTemplate = "select (select RelatedBean.* from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";
        String expectedSoql = "select (select RelatedBean.Id,RelatedBean.Name,RelatedBean.Description from SimpleBean.RelatedBeans) from SimpleBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(SimpleBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }

    @Test
    public void testRecursiveTypeReferences() throws Exception {
        String soqlTemplate = "select * from RecursiveBean where Id = '012345678901234'";
        String expectedSoql = "select Id,RecursiveBean.Id,RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id,RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.RecursiveBean.Id from RecursiveBean where Id = '012345678901234'";

        String soql = new SoqlBuilder(mappingContext.getEntityDescriptor(RecursiveBean.class)).soqlTemplate(soqlTemplate).build();
        assertThat(soql, is(equalTo(expectedSoql)));
    }
}
