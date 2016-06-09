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

import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.annotation.api.Scope;
import com.wesleyhome.annotation.api.ScopeType;

public class EntityInfoProcessor {

	protected ProcessorHelper annotationHelper;

	public EntityInfoProcessor(final ProcessorHelper helper) {
		annotationHelper = helper;
	}

	public EntityInfoImpl getEntityInfo(final TypeElement entityElement) {
		boolean isMappedSuperclass = isMappedSuperclass(entityElement);
		String fullClassName = getFullName(entityElement);
		String entityName = getEntityName(entityElement);
		ScopeType scopeType = getScope(entityElement);
		VariableElement idElement = getIdElements(entityElement);
		boolean cacheable = getCacheable(entityElement);
		return new EntityInfoImpl(entityElement, fullClassName, entityName, scopeType, idElement, cacheable,
			isMappedSuperclass);
	}

	private boolean isMappedSuperclass(final TypeElement entityElement) {
		AnnotationMirror mappedSuperclass = annotationHelper.getAnnotationMirror(entityElement, MappedSuperclass.class);
		return mappedSuperclass != null;
	}

	private boolean getCacheable(final TypeElement entityElement) {
		Object cacheable = annotationHelper.getAnnotationValue(entityElement, Cacheable.class, "value");
		return cacheable == null ? false : (boolean) cacheable;
	}

	/**
	 * @param entityElement
	 * @return
	 */
	private VariableElement getIdElements(final TypeElement entityElement) {
		List<VariableElement> annotatedElements = annotationHelper.getAnnotatedFields(entityElement, Id.class);
		if (annotatedElements.isEmpty()) {
			List<VariableElement> embeddedElementList = annotationHelper.getAnnotatedFields(entityElement, EmbeddedId.class);
			if (!embeddedElementList.isEmpty()) {
				annotatedElements.add(embeddedElementList.get(0));
			}
		}
		return annotatedElements.stream().findFirst().orElse(null);
	}

	/**
	 * @param entityElement
	 * @return
	 */
	private ScopeType getScope(final TypeElement entityElement) {
		Object scopeType = annotationHelper.getAnnotationValue(entityElement, Scope.class, "value");
		return scopeType == null ? ScopeType.NONE : ScopeType.valueOf(scopeType.toString());
	}

	/**
	 * @param entityElement
	 * @return
	 */
	private String getFullName(final TypeElement entityElement) {
		return entityElement.getQualifiedName().toString();
	}

	/**
	 * @param entityElement
	 * @return
	 */
	private String getEntityName(final TypeElement entityElement) {
		return entityElement.getSimpleName().toString();
	}
}
