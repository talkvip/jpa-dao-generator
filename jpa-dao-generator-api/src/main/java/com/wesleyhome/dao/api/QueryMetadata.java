package com.wesleyhome.dao.api;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

public interface QueryMetadata {

    List<Filter<?, ?>> getFilters();

    default boolean hasFilters() {
        return !getFilters().isEmpty();
    }

    <X, Y> QueryMetadata addFilter(Operators operator, SingularAttribute<X, Y> field, Y value);

    <X, Y> QueryMetadata addFilter(Operators operator, SingularAttribute<X, Y> field, Y value, BooleanOperator isAndFilter);

    static QueryMetadata create() {
        return new QueryMetadataImpl();
    }

    <Y> QueryMetadata addFilter(final Operators operator, final Path<Y> path, final Y value);

    <Y> QueryMetadata addFilter(final Operators operator, final Path<Y> path, final Y value, BooleanOperator isAndFilter);

    <Y> QueryMetadata addFilter(final Operators operator, final String dotPath, final Y value);

    <Y> QueryMetadata addFilter(final Operators operator, final String dotPath, final Y value, BooleanOperator isAndFilter);

    int getFirstResult();

    int getMaxResults();

    List<SortOrder<?, ?>> getOrders();

    default boolean hasOrders() {
        return !getOrders().isEmpty();
    }

    <Y> QueryMetadata addSortOrder(SingularAttribute<?, Y> path, boolean isAscending);

    QueryMetadata addSortOrder(String property, boolean isAscending);

    QueryMetadata addPageParameters(int firstResult, int maxResults);
}
