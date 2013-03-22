/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

/**
 * View definitions for controlling Jackson serialization.
 */
class SerializationViews {
    /**
     * View that is specified when serializing an object for {@link SimpleEntityManager#persist(Object)}.
     */
    static class Persist {
    }

    /**
     * View that is specified when serializing an object for {@link SimpleEntityManager#merge(Object)}}.
     */
    static class Merge {
    }

    /**
     * View that is never specified at serialization time so any field marked with this view will never be serialized.
     */
    static class Never {
    }
}
