package com.wesleyhome.dao.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.criteria.Root;

public abstract class BaseObjectDAO implements DAO {

	public BaseObjectDAO() {
		super();
	}

	protected abstract EntityManager getEntityManager();

	protected void applyQueryMetadata(final CriteriaQuery<?> cq, final Path<?> rootPath, final QueryMetadata queryMetadata,
		final Predicate... predicates) {
		if (!queryMetadata.getFilters().isEmpty()) {
			Predicate applyFilters = applyFilters(rootPath, queryMetadata);
			cq.where(combinePredicates(applyFilters, predicates));
		} else {
			applyPredicates(cq, predicates);
		}
		applySortOrder(cq, rootPath, queryMetadata);
	}

	private void applySortOrder(final CriteriaQuery<?> cq, final Path<?> rootPath, final QueryMetadata queryMetadata) {
		if (queryMetadata.hasOrders()) {
			List<SortOrder<?, ?>> sortOrderList = queryMetadata.getOrders();
			List<Order> orders = new ArrayList<>();
			for (SortOrder<?, ?> sortOrder : sortOrderList) {
				Order order = getOrder(cq, rootPath, sortOrder);
				orders.add(order);
			}
			cq.orderBy(orders);
		}
	}

	@SuppressWarnings("unchecked")
	private <X, Y> Order getOrder(final CriteriaQuery<?> cq, final Path<?> rootPath, final SortOrder<?, ?> sortOrder) {
		Path<X> parentPath = (Path<X>) rootPath;
		SortOrder<X, Y> o = (SortOrder<X, Y>) sortOrder;
		Path<Y> path = o.getPath(parentPath);
		return sortOrder.isAscending() ? getCriteriaBuilder().asc(path) : getCriteriaBuilder().desc(path);
	}

	private void applyPredicates(final CriteriaQuery<?> cq, final Predicate... predicates) {
		if (predicates != null && predicates.length > 0) {
			cq.where(predicates);
		}
	}

	private Predicate applyFilters(final Path<?> rootPath, final QueryMetadata queryMetadata) {
		Predicate predicate = null;
		List<Filter<?, ?>> filters = queryMetadata.getFilters();
		if (!filters.isEmpty()) {
			for (Filter<?, ?> filter : filters) {
				predicate = combinePredicates(predicate, _getPredicate(rootPath, filter), filter.getBooleanOperator());
			}
		}
		return predicate;
	}

	@SuppressWarnings("unchecked")
	private <X, Y> Predicate _getPredicate(final Path<?> rootPath, final Filter<?, ?> filter) {
		return getPredicate((Path<X>) rootPath, (Filter<X, Y>) filter);
	}

	private Predicate combinePredicates(final Predicate predicate, final Predicate... predicates) {
		if (predicates != null && predicates.length > 0) {
			Predicate p = predicate;
			for (Predicate p1 : predicates) {
				p = combinePredicates(p, p1, BooleanOperator.AND);
			}
			return p;
		}
		return predicate;
	}

	private Predicate combinePredicates(final Predicate original, final Predicate created, final BooleanOperator booleanOperator) {
		CriteriaBuilder cb = getCriteriaBuilder();
		if (original == null) {
			return created;
		}
		if (created == null) {
			return original;
		}
		switch (booleanOperator) {
			case OR:
				return cb.or(original, created);
			case AND:
			default:
				return cb.and(original, created);
		}
	}

	private <X, Y> Predicate getPredicate(final Path<X> rootPath, final Filter<X, Y> filter) {
		//	protected <T,X extends Comparable<X>> Predicate getPredicate(final Path<T> rootPath, final Filter<T, X> filter) {
		Path<Y> fieldPath = filter.getPath(rootPath);
		Operators operator = filter.getOperator();
		List<FilterValue<Y>> filterValues = filter.getFilterValues();
		if (filterValues.isEmpty()) {
			throw new IllegalStateException("Values cannot be empty");
		}
		switch (operator) {
			case IN:
				List<Y> fieldValues = convert(filterValues);
				return fieldPath.in(fieldValues);
			case BETWEEN:
				return getBetweenValued(fieldPath, filterValues);
			default:
				return getOtherPredicate(operator, fieldPath, filterValues);
		}
	}

	private <Y> List<Y> convert(final List<FilterValue<Y>> filterValues) {
		List<Y> list = new ArrayList<>();
		for (FilterValue<Y> filterValue : filterValues) {
			list.add(filterValue.getValue());
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private <Y> Predicate getBetweenValued(final Path<Y> fieldPath, final List<FilterValue<Y>> filterValues) {
		if (filterValues.size() % 2 == 1) {
			throw new IllegalArgumentException("Between comparison values must have a multiple of two values");
		}
		Predicate p = null;
		Class<? extends Y> javaType = fieldPath.getJavaType();
		Iterator<FilterValue<Y>> itr = filterValues.iterator();
		while (itr.hasNext()) {
			FilterValue<Y> val1 = itr.next();
			FilterValue<Y> val2 = itr.next();
			Y first = val1.getValue();
			Y second = val2.getValue();
			if (Date.class.isAssignableFrom(javaType)) {
				p = combinePredicates(p, between((Path<Date>) fieldPath, (Date) first, (Date) second), val1.getBooleanOperator());
			} else {
				p = combinePredicates(p, betweenNonComparable(fieldPath, first, second), val1.getBooleanOperator());
			}
		}
		return p;
	}

	@SuppressWarnings("unchecked")
	private <X, Y extends Comparable<Y>> Predicate betweenNonComparable(final Expression<X> path, final X first, final X second) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.and(isGreaterThanOrEqualTo((Expression<Y>) path, (Y) first),
			isLessThanOrEqualTo((Expression<Y>) path, (Y) second));
	}

	@SuppressWarnings("unchecked")
	private <X, Y> Predicate getOtherPredicate(final Operators operator, final Path<Y> fieldPath, final List<FilterValue<Y>> filterValues) {
		Class<? extends Y> javaType = fieldPath.getJavaType();
		Predicate p = null;
		for (FilterValue<Y> value : filterValues) {
			Y first = value.getValue();
			BooleanOperator booleanOperator = value.getBooleanOperator();
			switch (operator) {
				case CONTAINS:
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, contains((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					if (Number.class.isAssignableFrom(javaType)) {
						Expression<String> stringPath = fieldPath.as(String.class);
						p = combinePredicates(p, contains(stringPath, (String) first), booleanOperator);
					}
					break;
				case STARTS_WITH:
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, startsWith((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					if (Number.class.isAssignableFrom(javaType)) {
						Expression<String> stringPath = fieldPath.as(String.class);
						p = combinePredicates(p, startsWith(stringPath, (String) first), booleanOperator);
						break;
					}
					break;
				case ENDS_WITH:
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, endsWith((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					if (Number.class.isAssignableFrom(javaType)) {
						Expression<String> stringPath = fieldPath.as(String.class);
						p = combinePredicates(p, endsWith(stringPath, (String) first), booleanOperator);
						break;
					}
					return null;
				case EQUALS:
					if (first == null) {
						p = combinePredicates(p, isNull(fieldPath), booleanOperator);
						break;
					}
					if (first instanceof Date) {
						Date date = (Date) first;
						p = combinePredicates(p, dateEquals((Path<Date>) fieldPath, date), booleanOperator);
						break;
					}
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, equalsIgnoreCase((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					p = combinePredicates(p, equals(fieldPath, first), booleanOperator);
					break;
				default:
					p = getComparablePredicate(operator, fieldPath, filterValues);
			}
		}
		return p;
	}

	@SuppressWarnings("unchecked")
	private <Y, X extends Comparable<X>> Predicate getComparablePredicate(final Operators operator, final Path<Y> nonFieldPath,
		final List<FilterValue<Y>> filterValues) {
		Predicate p = null;
		Path<X> fieldPath = (Path<X>) nonFieldPath;
		for (FilterValue<Y> value : filterValues) {
			X first = (X) value.getValue();
			Class<X> javaType = (Class<X>) fieldPath.getJavaType();
			BooleanOperator booleanOperator = value.getBooleanOperator();
			switch (operator) {
				case GREATER_THAN:
					if (first instanceof Date) {
						Date date = (Date) first;
						p = combinePredicates(p, isAfter((Path<Date>) fieldPath, date), booleanOperator);
						break;
					}
					p = combinePredicates(p, isGreaterThan(fieldPath, first), booleanOperator);
					break;
				case GREATER_THAN_OR_EQUAL:
					if (first instanceof Date) {
						Date date = (Date) first;
						p = combinePredicates(p, isOnOrAfter((Path<Date>) fieldPath, date), booleanOperator);
						break;
					}
					p = combinePredicates(p, isGreaterThanOrEqualTo(fieldPath, first), booleanOperator);
					break;
				case LESS_THAN:
					if (first instanceof Date) {
						Date date = (Date) first;
						p = combinePredicates(p, isBefore((Path<Date>) fieldPath, date), booleanOperator);
						break;
					}
					p = combinePredicates(p, isLessThan(fieldPath, first), booleanOperator);
					break;
				case LESS_THAN_OR_EQUAL:
					if (first instanceof Date) {
						Date date = (Date) first;
						p = combinePredicates(p, isOnOrBefore((Path<Date>) fieldPath, date), booleanOperator);
						break;
					}
					p = combinePredicates(p, isLessThanOrEqualTo(fieldPath, first), booleanOperator);
					break;
				case CONTAINS:
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, contains((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					if (Number.class.isAssignableFrom(javaType)) {
						Expression<String> stringPath = fieldPath.as(String.class);
						p = combinePredicates(p, contains(stringPath, (String) first), booleanOperator);
					}
					break;
				case STARTS_WITH:
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, startsWith((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					if (Number.class.isAssignableFrom(javaType)) {
						Expression<String> stringPath = fieldPath.as(String.class);
						p = combinePredicates(p, startsWith(stringPath, (String) first), booleanOperator);
						break;
					}
					break;
				case ENDS_WITH:
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, endsWith((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					if (Number.class.isAssignableFrom(javaType)) {
						Expression<String> stringPath = fieldPath.as(String.class);
						p = combinePredicates(p, endsWith(stringPath, (String) first), booleanOperator);
						break;
					}
					return null;
				case IS_NULL:
					p = combinePredicates(p, isNull(fieldPath), booleanOperator);
					break;
				case IS_NOT_NULL:
					p = combinePredicates(p, isNotNull(fieldPath), booleanOperator);
					break;
				case NOT_EQUALS:
					if (first == null) {
						p = combinePredicates(p, isNotNull(fieldPath), booleanOperator);
						break;
					}
					if (first instanceof Date) {
						Date date = (Date) first;
						p = combinePredicates(p, getCriteriaBuilder().not(dateEquals((Path<Date>) fieldPath, date)), booleanOperator);
						break;
					}
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, getCriteriaBuilder().not(equalsIgnoreCase((Path<String>) fieldPath, (String) first)),
							booleanOperator);
						break;
					}
					p = combinePredicates(p, notEqual(fieldPath, first), booleanOperator);
					break;
				case EQUALS:
				default:
					if (first == null) {
						p = combinePredicates(p, isNull(fieldPath), booleanOperator);
						break;
					}
					if (first instanceof Date) {
						Date date = (Date) first;
						p = combinePredicates(p, dateEquals((Path<Date>) fieldPath, date), booleanOperator);
						break;
					}
					if (javaType.equals(String.class)) {
						p = combinePredicates(p, equalsIgnoreCase((Path<String>) fieldPath, (String) first), booleanOperator);
						break;
					}
					p = combinePredicates(p, equals(fieldPath, first), booleanOperator);
					break;
			}
		}
		return p;
	}

	protected <T> T getSingleResult(final CriteriaQuery<T> criteriaQuery) {
		try {
			return getTypedQuery(criteriaQuery).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	protected <T> T getCacheableSingleResult(final CriteriaQuery<T> criteriaQuery) {
		try {
			return makeCacheable(getTypedQuery(criteriaQuery)).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	protected <T> TypedQuery<T> getTypedQuery(final CriteriaQuery<T> criteriaQuery) {
		return getEntityManager().createQuery(criteriaQuery);
	}

	protected <T> List<T> getResultList(final CriteriaQuery<T> criteriaQuery) {
		return getResultList(criteriaQuery, null);
	}

	protected <T> List<T> getResultList(final CriteriaQuery<T> criteriaQuery, final QueryMetadata queryMetadata) {
		if (queryMetadata != null && queryMetadata.getFirstResult() >= 0 && queryMetadata.getMaxResults() >= 1) {
			return getPaginatedResultList(criteriaQuery, queryMetadata.getFirstResult(), queryMetadata.getMaxResults());
		}
		return getTypedQuery(criteriaQuery).getResultList();
	}

	protected <T> List<T> getCacheableResultList(final CriteriaQuery<T> criteriaQuery) {
		return getCacheableResultList(criteriaQuery, null);
	}

	protected <T> List<T> getCacheableResultList(final CriteriaQuery<T> criteriaQuery, final QueryMetadata queryMetadata) {
		if (queryMetadata != null && queryMetadata.getFirstResult() >= 0 && queryMetadata.getMaxResults() >= 1) {
			return getCacheablePaginatedResultList(criteriaQuery, queryMetadata.getFirstResult(), queryMetadata.getMaxResults());
		}
		return makeCacheable(this.getTypedQuery(criteriaQuery)).getResultList();
	}

	private <T> TypedQuery<T> makeCacheable(final TypedQuery<T> typedQuery) {
		return typedQuery.setHint(QueryHints.CACHEABLE, true).setHint(QueryHints.CACHE_REGION, getClass().getSimpleName());
	}

	protected CriteriaBuilder getCriteriaBuilder() {
		return getEntityManager().getCriteriaBuilder();
	}

	protected <V> Predicate equals(final Expression<V> path, final V value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.equal(path, value);
	}

	protected <V> Predicate notEqual(final Expression<V> path, final V value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.notEqual(path, value);
	}

	protected Predicate equalsIgnoreCase(final Expression<String> path, final String value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.equal(criteriaBuilder.lower(path), value.toLowerCase());
	}

	protected Predicate dateEquals(final Expression<Date> path, final Date value) {
		return this.equals(path, new java.sql.Date(value.getTime()));
	}

	protected <V extends Comparable<V>> Predicate betweenValues(final Expression<V> path, final V first, final V second) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.and(isGreaterThanOrEqualTo(path, first), isLessThanOrEqualTo(path, second));
	}

	protected Predicate isEffectiveWithOpenEndDate(final Date date, final Path<Date> effectiveDatePath,
		final Path<Date> expirationDatePath) {
		CriteriaBuilder cb = getCriteriaBuilder();
		Predicate datePredicate = cb.and(isOnOrBefore(effectiveDatePath, date),
			cb.or(cb.isNull(expirationDatePath), isOnOrAfter(expirationDatePath, date)));
		return datePredicate;
	}

	protected Predicate isEffectiveWithRequiredEndDate(final Date date, final Path<Date> effectiveDatePath,
		final Path<Date> expirationDatePath) {
		Predicate datePredicate = getCriteriaBuilder().and(isOnOrBefore(effectiveDatePath, date), isOnOrAfter(expirationDatePath, date));
		return datePredicate;
	}

	protected Predicate between(final Expression<Date> path, final Date start, final Date end) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		Date startDate = new java.sql.Date(start.getTime());
		Date endDate = new java.sql.Date(end.getTime());
		return criteriaBuilder.and(isOnOrAfter(path, startDate), isOnOrBefore(path, endDate));
	}

	protected Predicate isBefore(final Expression<Date> path, final Date value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.lessThan(path, new java.sql.Date(value.getTime()));
	}

	protected Predicate isAfter(final Expression<Date> path, final Date value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.greaterThan(path, new java.sql.Date(value.getTime()));
	}

	protected Predicate isOnOrBefore(final Expression<Date> path, final Date value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.lessThanOrEqualTo(path, new java.sql.Date(value.getTime()));
	}

	protected Predicate isOnOrAfter(final Expression<Date> path, final Date value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.greaterThanOrEqualTo(path, new java.sql.Date(value.getTime()));
	}

	protected <N extends Comparable<N>> Predicate isGreaterThan(final Expression<N> path, final N value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.greaterThan(path, value);
	}

	protected <N extends Comparable<N>> Predicate isLessThan(final Expression<N> path, final N value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.lessThan(path, value);
	}

	protected <N extends Comparable<N>> Predicate isGreaterThanOrEqualTo(final Expression<N> path, final N value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.greaterThanOrEqualTo(path, value);
	}

	protected <N extends Comparable<N>> Predicate isLessThanOrEqualTo(final Expression<N> path, final N value) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.lessThanOrEqualTo(path, value);
	}

	protected Predicate ilike(final Expression<String> x, final Expression<String> pattern) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.like(criteriaBuilder.lower(x), criteriaBuilder.lower(pattern));
	}

	protected Predicate ilike(final Expression<String> x, final Expression<String> pattern, final char escapeChar) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.like(criteriaBuilder.lower(x), criteriaBuilder.lower(pattern), escapeChar);
	}

	protected Predicate ilike(final Expression<String> x, final Expression<String> pattern, final Expression<Character> escapeChar) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.like(criteriaBuilder.lower(x), criteriaBuilder.lower(pattern), escapeChar);
	}

	protected Predicate ilike(final Expression<String> x, final String pattern) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.like(criteriaBuilder.lower(x), pattern.toLowerCase());
	}

	protected Predicate ilike(final Expression<String> x, final String pattern, final char escapeChar) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.like(criteriaBuilder.lower(x), pattern.toLowerCase(), escapeChar);
	}

	protected Predicate ilike(final Expression<String> x, final String pattern, final Expression<Character> escapeChar) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.like(criteriaBuilder.lower(x), pattern.toLowerCase(), escapeChar);
	}

	protected Predicate startsWith(final Expression<String> x, final String value) {
		return this.ilike(x, String.format("%s%%", value));
	}

	protected Predicate endsWith(final Expression<String> x, final String value) {
		return this.ilike(x, String.format("%%%s", value));
	}

	protected Predicate contains(final Expression<String> x, final String value) {
		return this.ilike(x, String.format("%%%s%%", value));
	}

	protected Predicate isNull(final Expression<?> path) {
		return getCriteriaBuilder().isNull(path);
	}

	protected Predicate isNotNull(final Expression<?> path) {
		return getCriteriaBuilder().isNotNull(path);
	}

	protected Expression<String> trim(final Expression<String> x) {
		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
		return criteriaBuilder.function("ltrim", String.class, criteriaBuilder.function("rtrim", String.class, x));
	}

	protected void sortQuery(final String sortField, final String sortOrder, final CriteriaBuilder cb,
		final CriteriaQuery<?> criteriaQuery, final Root<?> root) {
		Expression<?> path = getCaseInsensitivePath(cb, root.get(sortField));
		switch (sortOrder) {
			case "ASCENDING":
				criteriaQuery.orderBy(cb.asc(path));
				break;
			case "DESCENDING":
				criteriaQuery.orderBy(cb.desc(path));
				break;
		}
	}

	@SuppressWarnings("unchecked")
	protected Expression<?> getCaseInsensitivePath(final CriteriaBuilder cb, final Path<?> path) {
		Class<?> javaType = path.getJavaType();
		if (String.class.equals(javaType)) {
			return cb.lower((Expression<String>) path);
		}
		return path;
	}

	protected <T> List<T> getCacheablePaginatedResultList(final CriteriaQuery<T> criteriaQuery, final int firstRow, final int pageSize) {
		return makeCacheable(this.getTypedQuery(criteriaQuery)).setFirstResult(firstRow).setMaxResults(pageSize).getResultList();
	}

	protected <T> List<T> getPaginatedResultList(final CriteriaQuery<T> criteriaQuery, final int firstRow, final int pageSize) {
		return this.getTypedQuery(criteriaQuery).setFirstResult(firstRow).setMaxResults(pageSize).getResultList();
	}
}
