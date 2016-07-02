/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.dao.api;

public interface QueryHints {
    String CACHE_MODE = "org.hibernate.cacheMode";
    String CACHE_REGION = "org.hibernate.cacheRegion";
    String CACHEABLE = "org.hibernate.cacheable";
    String CALLABLE = "org.hibernate.callable";
    String COMMENT = "org.hibernate.comment";
    String FETCH_SIZE = "org.hibernate.fetchSize";
    String FLUSH_MODE = "org.hibernate.flushMode";
    String READ_ONLY = "org.hibernate.readOnly";
    String TIMEOUT_HIBERNATE = "org.hibernate.timeout";
    String TIMEOUT_JPA = "javax.persistence.query.timeout";
}
