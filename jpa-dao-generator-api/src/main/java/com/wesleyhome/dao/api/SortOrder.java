/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.dao.api;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class SortOrder<X, T> {

    private final SingularAttribute<X, T> field;
    private final String property;
    private final boolean isAscending;

    SortOrder(final SingularAttribute<X, T> field, final boolean isAscending) {
        this.field = field;
        this.property = null;
        this.isAscending = isAscending;
    }

    SortOrder(final String property, final boolean isAscending){
        this.field = null;
        this.property = property;
        this.isAscending = isAscending;
    }

    public Path<T> getPath(final Path<X> parentPath) {
        return this.field != null ? parentPath.get(this.field) : parentPath.get(property);
    }

    public boolean isAscending() {
        return isAscending;
    }
}
