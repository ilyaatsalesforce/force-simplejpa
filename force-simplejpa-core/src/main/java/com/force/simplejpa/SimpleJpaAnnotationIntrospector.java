/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerator;
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
import org.codehaus.jackson.map.util.BeanUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import java.io.IOException;
import java.lang.annotation.Annotation;

import static com.force.simplejpa.IntrospectionUtils.isRelationshipProperty;

/**
 * An {@link org.codehaus.jackson.map.AnnotationIntrospector} which understands a useful subset of JPA annotations. The
 * JPA annotations provide basic information for serializing and deserializing (the usual Jackson stuff) and also
 * provides entity relationship information which is used in building SOQL queries.
 *
 * @author davidbuccola
 */
class SimpleJpaAnnotationIntrospector extends NopAnnotationIntrospector {
    private final EntityDescriptorProvider descriptorProvider;
    private static final Class<?>[] NEVER_VIEWS = new Class<?>[]{SerializationViews.Never.class};
    private static final Class<?>[] PERSIST_VIEWS = new Class<?>[]{SerializationViews.Persist.class};
    private static final Class<?>[] MERGE_VIEWS = new Class<?>[]{SerializationViews.Merge.class};

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
        if (column != null && StringUtils.isNotEmpty(column.name())) {
            return column.name();
        }
        JoinColumn joinColumn = am.getAnnotation(JoinColumn.class);
        if (joinColumn != null && StringUtils.isNotEmpty(joinColumn.name())) {
            return joinColumn.name();
        }
        return null;
    }

    @Override
    public String findSerializablePropertyName(AnnotatedField af) {
        Column column = af.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotEmpty(column.name())) {
            return column.name();
        }
        JoinColumn joinColumn = af.getAnnotation(JoinColumn.class);
        if (joinColumn != null && StringUtils.isNotEmpty(joinColumn.name())) {
            return joinColumn.name();
        }
        return null;
    }

    @Override
    public String findSettablePropertyName(AnnotatedMethod am) {
        Column column = am.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotEmpty(column.name())) {
            return column.name();
        }
        JoinColumn joinColumn = am.getAnnotation(JoinColumn.class);
        if (joinColumn != null && StringUtils.isNotEmpty(joinColumn.name())) {
            return joinColumn.name();
        }
        return null;
    }

    @Override
    public String findPropertyNameForParam(AnnotatedParameter param) {
        Column column = param.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotEmpty(column.name())) {
            return column.name();
        }
        JoinColumn joinColumn = param.getAnnotation(JoinColumn.class);
        if (joinColumn != null && StringUtils.isNotEmpty(joinColumn.name())) {
            return joinColumn.name();
        }
        return null;
    }

    @Override
    public String findDeserializablePropertyName(AnnotatedField af) {
        return findSerializablePropertyName(af);
    }

    @Override
    public String findEnumValue(Enum<?> value) {
        return value.toString();
    }

    @Override
    public Class<?>[] findSerializationViews(Annotated a) {
        boolean insertable = isInsertable(a);
        boolean updatable = isUpdatable(a);

        if (!insertable || !updatable) {
            if (!insertable && !updatable) {
                return NEVER_VIEWS;   // Never serialized
            } else if (insertable) {
                return PERSIST_VIEWS; // Only serialized for persist (insert).
            } else {
                return MERGE_VIEWS;   // Only serialized for merge (update).
            }
        } else {
            return super.findSerializationViews(a);
        }
    }

    private String getPropertyName(Annotated annotated) {
        if (annotated instanceof AnnotatedField) {

            AnnotatedField field = (AnnotatedField) annotated;
            String name = findSerializablePropertyName(field);
            return (name != null) ? name : field.getName();

        } else if (annotated instanceof AnnotatedMethod) {

            AnnotatedMethod method = (AnnotatedMethod) annotated;
            String name = findGettablePropertyName(method);
            if (name == null) {
                if (method.getParameterCount() == 0) {
                    name = BeanUtil.okNameForGetter(method);
                } else {
                    name = BeanUtil.okNameForSetter(method);
                }
                if (name == null) {
                    throw new IllegalStateException("Unable to figure out the property name");
                }
            }
            return name;

        } else {
            throw new IllegalArgumentException("Unrecognized instance of 'Annotated'");
        }
    }

    private boolean isInsertable(Annotated annotated) {
        if (IntrospectionUtils.isNonInsertableStandardProperty(getPropertyName(annotated)))
            return false;

        Column column = annotated.getAnnotation(Column.class);
        if (column != null && !column.insertable()) {
            return false;
        }

        JoinColumn joinColumn = annotated.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.insertable()) {
            return false;
        }

        return true;
    }

    private boolean isUpdatable(Annotated annotated) {
        if (IntrospectionUtils.isNonUpdatableStandardProperty(getPropertyName(annotated)))
            return false;

        Column column = annotated.getAnnotation(Column.class);
        if (column != null && !column.updatable()) {
            return false;
        }

        JoinColumn joinColumn = annotated.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.updatable()) {
            return false;
        }

        return true;
    }

    @Override
    public Object findSerializer(Annotated annotated) {
        final EntityDescriptor descriptor = descriptorProvider.get(annotated.getRawType());
        if (isRelationshipProperty(annotated) && descriptor != null) {
            // Return a member-specific serializer that serializes just the entity's id instead of the whole entity.
            // For serialization of relationships (headed to database.com) we just serialize the id. This is important
            // to achieve the desired semantic for relating existing objects through the Salesforce REST API.
            return new JsonSerializer<Object>() {
                @Override
                public void serialize(Object object, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                    jgen.writeString(EntityUtils.getEntityId(descriptor, object));
                }
            };
        }
        return super.findSerializer(annotated);
    }
}
