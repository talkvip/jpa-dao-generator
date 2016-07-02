/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.codegen;

import com.squareup.javapoet.*;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.processors.annotations.Exists;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.criteria.*;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class ExistsMethodProcessor implements DaoMethodProcessor {

	@Override
	public Class<? extends Annotation> getAnnotationClassToProcess() {
		return Exists.class;
	}

	@Override
	public String getMethodName(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return "exists";
	}

	@Override
	public TypeName getReturnType(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return TypeName.BOOLEAN;
	}

	@Override
	public List<MethodParameter> getParameters(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return Arrays.asList(MethodParameter.of(entityInfo.getIdElement()));
	}

	@Override
	public boolean isClassLevelAnnotationProcessor() {
		return true;
	}

	@Override
	public boolean requiresDeveloperCode() {
		return false;
	}

	@Override
	public TypeName getSuperInterface(final EntityInfo entityInfo, final ProcessorHelper annotationHelper) {
		TypeElement idElementType = annotationHelper.getTypeElement(entityInfo.getIdElement().asType());
		ClassName idClassName = ClassName.get(idElementType);
		return ParameterizedTypeName.get(ClassName.get(com.wesleyhome.processors.interfaces.IExists.class), idClassName);
	}

	@Override
	public boolean hasInterfaceClass() {
		return true;
	}

	/**
	 * CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	 * CriteriaQuery&lt;Boolean> cq = cb.createQuery(Boolean.class);
	 */
	@Override
	public CodeBlock getMethodCode(final EntityInfo entityInfo, final Element element, final List<ParameterSpec> parameterList,
		final AnnotationMirror annotationMirror, final ProcessorHelper annotationHelper) {
		String metamodelName = entityInfo.getFullEntityClassName()+"_";
		ClassName metaModelClassName = ClassName.bestGuess(metamodelName);
		VariableElement idElement = entityInfo.getIdElement();
		return CodeBlock
			.builder()
			.addStatement("$T cb = getEntityManager().getCriteriaBuilder()", CriteriaBuilder.class)
			.addStatement("$T<Boolean> cq = cb.createQuery(Boolean.class)", CriteriaQuery.class)
			.addStatement("$T<$T> root = cq.from($T.class)", Root.class, entityInfo.getTypeElement(), entityInfo.getTypeElement())
			.addStatement("$T<$T> keyPath = root.get($T.$L)", Path.class, idElement, metaModelClassName, idElement)
			.addStatement("$T<Long> countExpression = cb.count(keyPath)", Expression.class)
			.addStatement("$T countGreaterThanZero = cb.greaterThan(countExpression, 0L)", Predicate.class)
			.addStatement("Predicate keyEquals = cb.equal(keyPath, $L)", idElement)
			.addStatement("return getSingleResult(cq.select(countGreaterThanZero).where(keyEquals))")
			.build();
	}
}
