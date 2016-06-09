package com.wesleyhome.processors.codegen;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.processors.annotations.FindAll;

public class FindAllMethodProcessor implements DaoMethodProcessor {

	@Override
	public Class<? extends Annotation> getAnnotationClassToProcess() {
		return FindAll.class;
	}

	@Override
	public String getMethodName(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return "findAll";
	}

	@Override
	public TypeName getReturnType(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		ClassName entityClassName = ClassName.get(entityInfo.getTypeElement());
		return ParameterizedTypeName.get(ClassName.get(List.class), entityClassName);
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
		ClassName entityClassName = ClassName.get(entityInfo.getTypeElement());
		return ParameterizedTypeName.get(ClassName.get(com.wesleyhome.processors.interfaces.IFindAll.class), entityClassName);
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
		 * CriteriaQuery<AccountingBook> cq = cb.createQuery(AccountingBook.class);
		 * cq.from(AccountingBook.class);
		 * return this.getResultList(cq);
		 */
		String methodCall = entityInfo.isCacheable() ? "getCacheableResultList" : "getResultList";
		return CodeBlock.builder()
			.addStatement("$T cb = this.getCriteriaBuilder()", CriteriaBuilder.class)
			.addStatement("$T<$T> cq = cb.createQuery($T.class)", CriteriaQuery.class, entityInfo.getTypeElement(),
				entityInfo.getTypeElement())
			.addStatement("cq.from($T.class)", entityInfo.getTypeElement())
			.addStatement("return this.$L(cq)", methodCall)
			.build();
	}
}
