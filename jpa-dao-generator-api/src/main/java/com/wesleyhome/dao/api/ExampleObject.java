package com.wesleyhome.dao.api;

import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;

public abstract class ExampleObject<T extends ExampleObject<T>> {

    protected T self;
    private QueryMetadata queryMetadata;
    private BooleanOperator nextOperator;

    @SuppressWarnings("unchecked")
    protected ExampleObject() {
        super();
        queryMetadata = QueryMetadata.create();
        nextOperator = BooleanOperator.AND;
        self = (T) this;
    }

    protected <X, Y> QueryMetadata addFilter(final Operators operator, final SingularAttribute<X, Y> field, final Y value) {
        return queryMetadata.addFilter(operator, field, value, nextOperator);
    }

    protected <X, Y> QueryMetadata addInFilter(final SingularAttribute<X, Y> field, final Collection<Y> values) {
        for (Y y : values) {
            queryMetadata.addFilter(Operators.IN, field, y);
        }
        return queryMetadata;
    }

    public <X, Y> T addSortOrder(final SingularAttribute<X, Y> field, final boolean isAscending) {
        queryMetadata.addSortOrder(field, isAscending);
        return self;
    }

    public T addSortOrder(final String property, final boolean isAscending) {
        queryMetadata.addSortOrder(property, isAscending);
        return self;
    }

    public T addPageParameters(final int firstResult, final int maxResults) {
        queryMetadata.addPageParameters(firstResult, maxResults);
        return self;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public T and() {
        nextOperator = BooleanOperator.AND;
        return self;
    }

    public T or() {
        nextOperator = BooleanOperator.OR;
        return self;
    }
}
