package com.wesleyhome.dao.api;

import javax.persistence.criteria.Predicate.BooleanOperator;

public class FilterValue<T> {
    private final T value;
    private final BooleanOperator booleanOperator;

    FilterValue(final T value, final BooleanOperator booleanOperator) {
        super();
        this.value = value;
        this.booleanOperator = booleanOperator;
    }

    public T getValue() {
        return value;
    }

    BooleanOperator getBooleanOperator() {
        return booleanOperator;
    }
}
