package com.wesleyhome.dao.api;

import java.util.List;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.metamodel.SingularAttribute;

public interface QueryMetadata {

	List<Filter<?, ?>> getFilters();

	public default boolean hasFilters() {
		return !getFilters().isEmpty();
	}

	<X, Y> QueryMetadata addFilter(Operators operator, SingularAttribute<X, Y> field, Y value);

	<X, Y> QueryMetadata addFilter(Operators operator, SingularAttribute<X, Y> field, Y value, BooleanOperator isAndFilter);

	public static QueryMetadata create() {
		return new QueryMetadataImpl();
	}

	<X, Y> QueryMetadata addFilter(final Operators operator, final Path<Y> path, final Y value);

	<X, Y> QueryMetadata addFilter(final Operators operator, final Path<Y> path, final Y value, BooleanOperator isAndFilter);

	<X, Y> QueryMetadata addFilter(final Operators operator, final String dotPath, final Y value);

	<X, Y> QueryMetadata addFilter(final Operators operator, final String dotPath, final Y value, BooleanOperator isAndFilter);

	int getFirstResult();

	int getMaxResults();

	List<SortOrder<?, ?>> getOrders();

	public default boolean hasOrders() {
		return !getOrders().isEmpty();
	}

	<Y> QueryMetadata addSortOrder(SingularAttribute<?, Y> path, boolean isAscending);

	QueryMetadata addPageParameters(int firstResult, int maxResults);
}
