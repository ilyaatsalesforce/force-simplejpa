/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;

import static net.davidbuccola.simplejpa.IntrospectionUtils.isRelationshipField;

/**
 * An {@link org.codehaus.jackson.map.AnnotationIntrospector} which understands a useful subset of JPA annotations. The
 * JPA annotations provide basic information for serializing and deserializing (the usual Jackson stuff) and also
 * provides entity relationship information which is used in building SOQL queries.
 *
 * @author davidbuccola
 */
class SimpleJpaAnnotationIntrospector extends NopAnnotationIntrospector {
    private final EntityDescriptorProvider descriptorProvider;

    SimpleJpaAnnotationIntrospector(EntityDescriptorProvider descriptorProvider) {
        this.descriptorProvider = descriptorProvider;
    }

    @Override
    public boolean isHandled(Annotation annotation) {
        Class<?> clazz = annotation.annotationType();
        if (Entity.class.getPackage().equals(clazz.getPackage()))
            return true;

        if (clazz == JsonCachable.class)
            return true;

        return false;
    }

    @Override
    public String findRootName(AnnotatedClass ac) {
        Entity annotation = ac.getAnnotation(Entity.class);
        return (annotation == null) ? null : annotation.name();
    }

    @Override
    public String findTypeName(AnnotatedClass ac) {
        Entity annotation = ac.getAnnotation(Entity.class);
        return (annotation == null) ? null : annotation.name();
    }

    @Override
    public Boolean findIgnoreUnknownProperties(AnnotatedClass ac) {
        return true;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember member) {
        return isIgnorable(member);
    }

    @Override
    public boolean isIgnorableMethod(AnnotatedMethod method) {
        return isIgnorable(method);
    }

    @Override
    public boolean isIgnorableConstructor(AnnotatedConstructor constructor) {
        return isIgnorable(constructor);
    }

    @Override
    public boolean isIgnorableField(AnnotatedField field) {
        return isIgnorable(field);
    }

    private boolean isIgnorable(Annotated a) {
        Transient annotation = a.getAnnotation(Transient.class);
        return annotation != null;
    }

    @Override
    public String findGettablePropertyName(AnnotatedMethod am) {
        Column column = am.getAnnotation(Column.class);
        if (column != null)
            return column.name();

        JoinColumn joinColumn = am.getAnnotation(JoinColumn.class);
        if (joinColumn != null)
            return joinColumn.name();

        return null;
    }

    @Override
    public String findSerializablePropertyName(AnnotatedField af) {
        Column column = af.getAnnotation(Column.class);
        if (column != null)
            return column.name();

        JoinColumn joinColumn = af.getAnnotation(JoinColumn.class);
        if (joinColumn != null)
            return joinColumn.name();

        return null;
    }

    @Override
    public String findSettablePropertyName(AnnotatedMethod am) {
        Column column = am.getAnnotation(Column.class);
        if (column != null)
            return column.name();

        JoinColumn joinColumn = am.getAnnotation(JoinColumn.class);
        if (joinColumn != null)
            return joinColumn.name();

        return null;
    }

    @Override
    public String findDeserializablePropertyName(AnnotatedField af) {
        Column column = af.getAnnotation(Column.class);
        if (column != null)
            return column.name();

        JoinColumn joinColumn = af.getAnnotation(JoinColumn.class);
        if (joinColumn != null)
            return joinColumn.name();

        return null;
    }

    @Override
    public String findPropertyNameForParam(AnnotatedParameter param) {
        Column column = param.getAnnotation(Column.class);
        if (column != null)
            return column.name();

        JoinColumn joinColumn = param.getAnnotation(JoinColumn.class);
        if (joinColumn != null)
            return joinColumn.name();

        return null;
    }

    @Override
    public String findEnumValue(Enum<?> value) {
        return value.toString();
    }

    @Override
    public Object findSerializer(Annotated annotated) {
        final EntityDescriptor descriptor = descriptorProvider.get(annotated.getRawType());
        if (isRelationshipField(annotated) && descriptor != null) {
            // Return a member-specific serializer that serializes just the entity's id instead of the whole entity.
            // For serialization of relationships (headed to database.com) we just serialize the id. This is important
            // to achieve the desired semantic for relating existing objects through the database.com REST API.
            return new JsonSerializer<Object>() {
                @Override
                public void serialize(Object object, JsonGenerator jgen, SerializerProvider provider)
                        throws IOException {
                    jgen.writeString(getEntityId(descriptor.getIdProperty(), object));
                }
            };
        }
        return super.findSerializer(annotated);
    }

    /**
     * Gets the ID property of an entity instance.
     *
     * @param idProperty definition of the idProperty
     * @param instance   the entity instance from which to get the id
     *
     * @return the id
     */
    private static String getEntityId(BeanPropertyDefinition idProperty, Object instance) {
        try {
            if (idProperty.hasGetter()) {
                return idProperty.getGetter().getAnnotated().invoke(instance).toString();
            } else if (idProperty.hasField()) {
                return idProperty.getField().getAnnotated().get(instance).toString();
            } else
                throw new IllegalStateException("There is no way to set the entity id");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
