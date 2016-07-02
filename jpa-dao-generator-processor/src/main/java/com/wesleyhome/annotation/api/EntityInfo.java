/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.annotation.api;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public interface EntityInfo extends Comparable<EntityInfo> {

	/**
	 * If the class has no DAO Generator annotations, the DAO's will not be generated.
	 * @return
	 */
	boolean processEntity();

	void applySuperclass(EntityInfo info);

	boolean hasSuperClass();

	boolean isBaseClass();

	boolean isEntityClass();

	boolean isSuperClass(EntityInfo info);

	String packageName();

	TypeElement getTypeElement();

	String getFullEntityClassName();

	String getEntityName();

	ScopeType getScopeType();

	VariableElement getIdElement();

	EntityInfo getSuperClassInfo();

	List<EntityInfo> getSubClassList();

	boolean isCacheable();
}
