package com.wesleyhome.dao.api;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate.BooleanOperator;

class DotPathFilter<X, T> extends AbstractFilter<X, T> {
	private String dotPath;

	protected DotPathFilter(final Operators operator, final String dotPath, final BooleanOperator booleanOperator) {
		super(operator, booleanOperator);
		if (dotPath == null || dotPath.trim().length() == 0) {
			throw new IllegalArgumentException("DotPath cannot be empty");
		}
		this.dotPath = dotPath;
	}

	@Override
	public Path<T> getPath(final Path<X> parentPath) {
		List<String> pathList = Arrays.asList(dotPath.split("\\."));
		Queue<String> pathQueue = new ArrayDeque<String>(pathList);
		return getPath(parentPath, pathQueue);
	}

	private Path<T> getPath(final Path<X> parentPath, final Queue<String> pathList) {
		if (pathList.isEmpty()) {
			throw new IllegalArgumentException("pathArray cannot be empty");
		}
		String path = pathList.poll();
		Path<T> currentPath = parentPath.get(path);
		while (!pathList.isEmpty()) {
			path = pathList.poll();
			currentPath = currentPath.get(path);
		}
		return currentPath;
	}
}
