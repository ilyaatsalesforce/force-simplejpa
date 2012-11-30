/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;

/**
 * Miscellaneous utilities for asking questions about entities and their metadata.
 *
 * @author davidbuccola
 */
abstract class IntrospectionUtils {
    private static final Set<String> STANDARD_FIELD_NAMES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            "Id", "Name", "CreatedBy", "CreatedDate", "LastModifiedBy", "LastModifiedDate", "Owner",
            "MasterLabel", "DeveloperName", "Language")));

    private IntrospectionUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    static boolean isPropertyOfCustomEntity(AnnotatedMember member) {
        return getEntityName(member.getDeclaringClass()).endsWith("__c");
    }

    static String getEntityNamespace(AnnotatedMember member) {
        String entityName = getEntityName(member.getDeclaringClass());
        String entityNameSansSuffix = entityName.substring(0, entityName.lastIndexOf("__"));
        int p = entityNameSansSuffix.lastIndexOf("__");
        return p > 0 ? entityNameSansSuffix.substring(0, p) : null;
    }

    static boolean isMissingNamespace(String name) {
        return !name.contains("__");
    }

    static boolean isNotStandardProperty(String name) {
        return !STANDARD_FIELD_NAMES.contains(name);
    }

    static boolean isRelationshipField(Annotated annotated) {
        if (!(annotated instanceof AnnotatedMember))
            return false;

        AnnotatedMember annotatedMember = (AnnotatedMember) annotated;
        if (isRelationshipAnnotationPresent(annotatedMember))
            return true;

        Field relatedField = getRelatedField(annotatedMember);
        if (relatedField != null && isRelationshipAnnotationPresent(relatedField))
            return true;

        return false;
    }

    private static String getEntityName(Class<?> clazz) {
        Entity entity = clazz.getAnnotation(Entity.class);
        if (entity != null) return entity.name();
        else
            return clazz.getSimpleName();
    }

    private static boolean isRelationshipAnnotationPresent(Annotated annotated) {
        return annotated.hasAnnotation(ManyToOne.class) || annotated.hasAnnotation(OneToOne.class);
    }

    private static boolean isRelationshipAnnotationPresent(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
    }

    /**
     * Find the {@link Field} that corresponds to the annotated member. When the annotated member is a setter or getter
     * this is used to find the corresponding field definition.
     *
     * @param member an annotated member
     *
     * @return a field that corresponds to the annotated member
     */
    private static Field getRelatedField(AnnotatedMember member) {
        if (member instanceof AnnotatedField)
            return ((AnnotatedField) member).getAnnotated();

        String methodName = member.getName();
        if (!(methodName.startsWith("get") || methodName.startsWith("set")))
            return null;

        String relatedFieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        try {
            return member.getDeclaringClass().getDeclaredField(relatedFieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
