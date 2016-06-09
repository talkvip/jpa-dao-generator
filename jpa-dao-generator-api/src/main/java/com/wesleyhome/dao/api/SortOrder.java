package com.wesleyhome.dao.api;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class SortOrder<X, T> {

	private final SingularAttribute<X, T> field;
	private final boolean isAscending;

	public SortOrder(final SingularAttribute<X, T> field, final boolean isAscending) {
		this.field = field;
		this.isAscending = isAscending;
	}

	public Path<T> getPath(final Path<X> parentPath) {
		return parentPath.get(this.field);
	}

	public boolean isAscending() {
		return isAscending;
	}
}
