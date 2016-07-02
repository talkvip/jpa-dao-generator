/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.codegen;

import com.squareup.javapoet.*;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.dao.api.QueryMetadata;
import com.wesleyhome.processors.annotations.FindByExampleCount;
import com.wesleyhome.processors.interfaces.IFindByExampleCount;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class FindByExampleCountMethodProcessor implements DaoMethodProcessor {

	@Override
	public Class<? extends Annotation> getAnnotationClassToProcess() {
		return FindByExampleCount.class;
	}

	@Override
	public String getMethodName(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return "getCount";
	}

	@Override
	public TypeName getReturnType(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return ClassName.get(Long.class);
	}

	@Override
	public List<MethodParameter> getParameters(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		ClassName exampleClassName = ExampleClassGenerator.getExampleClassName(entityInfo);
		MethodParameter methodParameter = MethodParameter.of(exampleClassName, "example");
		return Arrays.asList(methodParameter);
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
		ClassName exampleClassName = ExampleClassGenerator.getExampleClassName(entityInfo);
		return ParameterizedTypeName.get(ClassName.get(IFindByExampleCount.class), exampleClassName);
	}

	@Override
	public boolean hasInterfaceClass() {
		return true;
	}

	@Override
	public CodeBlock getMethodCode(final EntityInfo entityInfo, final Element element, final List<ParameterSpec> parameterList,
		final AnnotationMirror annotationMirror, final ProcessorHelper annotationHelper) {
		/*
		 * CriteriaBuilder cb = this.getCriteriaBuilder();
		 * CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		 * Root<AccountingBook> root = cq.from(AccountingBook.class);
		 * Expression<Long> countExpression = cb.count(root);
		 * return this.getSingleResult(cq.select(countExpression));
		 */
		return CodeBlock.builder()
			.addStatement("$T cb = this.getCriteriaBuilder()", CriteriaBuilder.class)
			.addStatement("$T<Long> cq = cb.createQuery(Long.class)", CriteriaQuery.class)
			.addStatement("$T<$T> root = cq.from($T.class)", Root.class, entityInfo.getTypeElement(), entityInfo.getTypeElement())
			.addStatement("$T metadata = example.getQueryMetadata()", QueryMetadata.class)
			.addStatement("this.applyQueryMetadata(cq, root, metadata)")
			.addStatement("$T<Long> countExpression = cb.count(root);", Expression.class)
			.addStatement("return this.getSingleResult(cq.select(countExpression))")
			.build();
	}
}
