/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.dao.api;

import javax.persistence.criteria.Predicate.BooleanOperator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractFilter<X, T> implements Filter<X, T> {
    private final Operators operator;
    private final List<FilterValue<T>> filterValues;
    private final BooleanOperator booleanOperator;

    AbstractFilter(final Operators operator, final BooleanOperator booleanOperator) {
        super();
        this.operator = operator;
        this.booleanOperator = booleanOperator;
        this.filterValues = new ArrayList<>();
    }

    @Override
    public final void addValue(final T value, final BooleanOperator booleanOperator) {
        this.filterValues.add(new FilterValue<>(value, booleanOperator));
    }

    @Override
    public final Operators getOperator() {
        return this.operator;
    }

    @Override
    public final List<FilterValue<T>> getFilterValues() {
        return Collections.unmodifiableList(this.filterValues);
    }

    @Override
    public final BooleanOperator getBooleanOperator() {
        return booleanOperator;
    }
}
