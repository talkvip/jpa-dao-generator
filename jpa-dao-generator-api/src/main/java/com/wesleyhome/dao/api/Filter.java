package com.wesleyhome.dao.api;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate.BooleanOperator;
import java.util.List;

interface Filter<X, T> {
    Operators getOperator();

    Path<T> getPath(Path<X> parentPath);

    List<FilterValue<T>> getFilterValues();

    void addValue(T value, final BooleanOperator booleanOperator);

    BooleanOperator getBooleanOperator();
}
