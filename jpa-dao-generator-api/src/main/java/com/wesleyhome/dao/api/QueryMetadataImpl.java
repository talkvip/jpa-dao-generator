package com.wesleyhome.dao.api;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

class QueryMetadataImpl implements QueryMetadata {

    private final Map<Integer, Filter<?, ?>> fieldFilters;
    private final Map<Integer, Filter<?, ?>> pathFilters;
    private final List<SortOrder<?, ?>> orders;
    private int firstResult;
    private int maxResults;

    QueryMetadataImpl() {
        fieldFilters = new LinkedHashMap<>();
        pathFilters = new LinkedHashMap<>();
        orders = new ArrayList<>();
    }

    @Override
    public List<Filter<?, ?>> getFilters() {
        List<Filter<?, ?>> list = new ArrayList<>(fieldFilters.values());
        return Collections.unmodifiableList(list);
    }

    @Override
    public <Y> QueryMetadata addFilter(final Operators operator, final Path<Y> path, final Y value) {
        return addFilter(operator, path, value, BooleanOperator.AND);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y> QueryMetadata addFilter(final Operators operator, final SingularAttribute<X, Y> field, final Y value,
                                          final BooleanOperator booleanOperator) {
        int hash = hash(operator, field);
        Filter<X, Y> filter = (Filter<X, Y>) fieldFilters.get(hash);
        if (filter == null) {
            filter = new AttributeFilterImpl<>(operator, field, booleanOperator);
            fieldFilters.put(hash, filter);
        }
        filter.addValue(value, booleanOperator);
        return this;
    }

    @Override
    public <X, Y> QueryMetadata addFilter(final Operators operator, final SingularAttribute<X, Y> field, final Y value) {
        return addFilter(operator, field, value, BooleanOperator.AND);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Y> QueryMetadata addFilter(final Operators operator, final Path<Y> path, final Y value,
                                          final BooleanOperator booleanOperator) {
        int hash = hash(operator, path);
        Filter<?, Y> filter = (Filter<?, Y>) pathFilters.get(hash);
        if (filter == null) {
            filter = new PathFilter<>(operator, path, booleanOperator);
            fieldFilters.put(hash, filter);
        }
        filter.addValue(value, booleanOperator);
        return this;
    }

    @Override
    public <Y> QueryMetadata addFilter(final Operators operator, final String path, final Y value) {
        return addFilter(operator, path, value, BooleanOperator.AND);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Y> QueryMetadata addFilter(final Operators operator, final String path, final Y value,
                                          final BooleanOperator booleanOperator) {
        int hash = hash(operator, path);
        Filter<?, Y> filter = (Filter<?, Y>) pathFilters.get(hash);
        if (filter == null) {
            filter = new DotPathFilter<>(operator, path, booleanOperator);
            fieldFilters.put(hash, filter);
        }
        filter.addValue(value, booleanOperator);
        return this;
    }

    private int hash(final Operators operator, final Object field) {
        // default hash prime
        final int prime = 31;
        int result = 1;
        result = prime * result + operator.hashCode();
        result = prime * result + field.hashCode();
        return result;
    }

    @Override
    public <Y> QueryMetadata addSortOrder(final SingularAttribute<?, Y> path, final boolean isAscending) {
        SortOrder<?, Y> sortOrder = new SortOrder<>(path, isAscending);
        orders.add(sortOrder);
        return this;
    }

    @Override
    public QueryMetadata addSortOrder(final String property, final boolean isAscending) {
        SortOrder<?, ?> sortOrder = new SortOrder<>(property, isAscending);
        orders.add(sortOrder);
        return this;
    }


    @Override
    public QueryMetadata addPageParameters(final int firstResult, final int maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public List<SortOrder<?, ?>> getOrders() {
        return orders;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }
}
