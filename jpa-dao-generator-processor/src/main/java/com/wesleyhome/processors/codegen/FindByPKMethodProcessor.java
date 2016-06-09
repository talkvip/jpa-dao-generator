package com.wesleyhome.processors.codegen;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.processors.annotations.FindByPK;

public class FindByPKMethodProcessor implements DaoMethodProcessor {

	@Override
	public Class<? extends Annotation> getAnnotationClassToProcess() {
		return FindByPK.class;
	}

	@Override
	public String getMethodName(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return "findByPK";
	}

	@Override
	public TypeName getReturnType(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return ClassName.get(entityInfo.getTypeElement());
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
		ClassName entityClassName = ClassName.get(entityInfo.getTypeElement());
		TypeElement idElementType = annotationHelper.getTypeElement(entityInfo.getIdElement().asType());
		ClassName idClassName = ClassName.get(idElementType);
		return ParameterizedTypeName.get(ClassName.get(com.wesleyhome.processors.interfaces.IFindByPK.class), idClassName, entityClassName);
	}

	@Override
	public boolean hasInterfaceClass() {
		return true;
	}

	@Override
	public CodeBlock getMethodCode(final EntityInfo entityInfo, final Element element, final List<ParameterSpec> parameterList,
		final AnnotationMirror annotationMirror, final ProcessorHelper annotationHelper) {
		return CodeBlock
			.builder()
			.addStatement("return getEntityManager().find($T.class, $L)", entityInfo.getTypeElement(), parameterList.get(0).name)
			.build();
	}
}
