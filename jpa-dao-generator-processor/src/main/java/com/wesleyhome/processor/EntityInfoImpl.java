/*
 * Copyright 2014 Justin Wesley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wesleyhome.processor;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.apache.commons.lang3.StringUtils;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.ScopeType;

/**
 * The <code>GeneratedQuery</code> class is a
 *
 * @author
 * @since
 */
class EntityInfoImpl implements EntityInfo {

	private final TypeElement typeElement;
	private final String fullEntityClassName;
	private final String entityName;
	private final ScopeType scopeType;
	private VariableElement idElement;
	private EntityInfo superClassInfo;
	private final List<EntityInfo> subClassList;
	private final boolean mappedSuperclass;
	private final boolean cacheable;

	/**
	 * @param typeElement
	 * @param subPackageName
	 * @param fullEntityClassName
	 * @param entityName
	 * @param scopeType
	 * @param customQueryInfoList
	 * @param queryParameterInfoList
	 * @param mappedSuperclass
	 */
	EntityInfoImpl(final TypeElement typeElement, final String fullEntityClassName, final String entityName,
		final ScopeType scopeType, final VariableElement idElement, final boolean cacheable, final boolean mappedSuperclass) {
		super();
		this.typeElement = typeElement;
		this.fullEntityClassName = fullEntityClassName;
		this.entityName = entityName;
		this.scopeType = scopeType;
		this.idElement = idElement;
		this.cacheable = cacheable;
		this.mappedSuperclass = mappedSuperclass;
		subClassList = new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#processEntity()
	 */
	@Override
	public boolean processEntity() {
		return !mappedSuperclass;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#applySuperclass(com.travelers.smart.dao.processor.model.EntityInfo)
	 */
	@Override
	public void applySuperclass(final EntityInfo info) {
		superClassInfo = info;
		info.getSubClassList().add(this);
		if (idElement == null) {
			idElement = info.getIdElement();
		}
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#hasSuperClass()
	 */
	@Override
	public boolean hasSuperClass() {
		return superClassInfo != null;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#isBaseClass()
	 */
	@Override
	public boolean isBaseClass() {
		return superClassInfo == null;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#isEntityClass()
	 */
	@Override
	public boolean isEntityClass() {
		return !mappedSuperclass;
	}

	@Override
	public int compareTo(final EntityInfo o) {
		if (o.isSuperClass(this)) {
			return -1;
		}
		if (isSuperClass(o)) {
			return 1;
		}
		return fullEntityClassName.compareTo(o.getFullEntityClassName());
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#isSuperClass(com.travelers.smart.dao.processor.model.IEntityInfo)
	 */
	@Override
	public boolean isSuperClass(final EntityInfo info) {
		if (superClassInfo == null) {
			return false;
		}
		if (superClassInfo == info) {
			return true;
		}
		return superClassInfo.isSuperClass(info);
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#packageName()
	 */
	@Override
	public String packageName() {
		return StringUtils.removeEnd(fullEntityClassName, "." + entityName);
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#getTypeElement()
	 */
	@Override
	public TypeElement getTypeElement() {
		return typeElement;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#getFullEntityClassName()
	 */
	@Override
	public String getFullEntityClassName() {
		return fullEntityClassName;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#getEntityName()
	 */
	@Override
	public String getEntityName() {
		return entityName;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#getScopeType()
	 */
	@Override
	public ScopeType getScopeType() {
		return scopeType;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#getIdElement()
	 */
	@Override
	public VariableElement getIdElement() {
		return idElement;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#getSuperClassInfo()
	 */
	@Override
	public EntityInfo getSuperClassInfo() {
		return superClassInfo;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#getSubClassList()
	 */
	@Override
	public List<EntityInfo> getSubClassList() {
		return subClassList;
	}

	/* (non-Javadoc)
	 * @see com.travelers.smart.dao.processor.model.IEntityInfo#isCacheable()
	 */
	@Override
	public boolean isCacheable() {
		return cacheable;
	}
}
