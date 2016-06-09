package com.wesleyhome.annotation.api;

import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

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
