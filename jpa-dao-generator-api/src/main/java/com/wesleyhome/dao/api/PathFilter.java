/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.dao.api;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate.BooleanOperator;

class PathFilter<X, T> extends AbstractFilter<X, T> {
    private final Path<T> path;

    PathFilter(final Operators operator, final Path<T> path, final BooleanOperator booleanOperator) {
        super(operator, booleanOperator);
        this.path = path;
    }

    @Override
    public Path<T> getPath(final Path<X> parentPath) {
        return this.path;
    }
}
