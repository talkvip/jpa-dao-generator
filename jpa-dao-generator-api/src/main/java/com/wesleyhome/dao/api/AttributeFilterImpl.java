/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.dao.api;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.metamodel.SingularAttribute;

class AttributeFilterImpl<X, T> extends AbstractFilter<X, T> {
    private final SingularAttribute<X, T> field;

    AttributeFilterImpl(final Operators operator, final SingularAttribute<X, T> field, final BooleanOperator booleanOperator) {
        super(operator, booleanOperator);
        this.field = field;
    }

    @Override
    public Path<T> getPath(final Path<X> parentPath) {
        return parentPath.get(this.field);
    }
}
