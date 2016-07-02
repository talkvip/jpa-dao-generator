package com.wesleyhome.processors.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.processors.annotations.FindAllCount;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class FindAllCountMethodProcessor implements DaoMethodProcessor {

	@Override
	public Class<? extends Annotation> getAnnotationClassToProcess() {
		return FindAllCount.class;
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
		return new ArrayList<>();
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
		return ClassName.get(com.wesleyhome.processors.interfaces.IFindAllCount.class);
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
			.addStatement("$T<Long> countExpression = cb.count(root);", Expression.class)
			.addStatement("return this.getSingleResult(cq.select(countExpression))")
			.build();
	}
}
