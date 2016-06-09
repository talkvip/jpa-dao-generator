package com.wesleyhome.annotation.api;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public interface DaoMethodProcessor {

	Class<? extends Annotation> getAnnotationClassToProcess();

	String getMethodName(EntityInfo entityInfo, Element element, AnnotationMirror annotationMirror, ProcessorHelper annotationHelper);

	TypeName getReturnType(EntityInfo entityInfo, Element element, AnnotationMirror annotationMirror, ProcessorHelper annotationHelper);

	List<MethodParameter> getParameters(EntityInfo entityInfo, Element element, AnnotationMirror annotationMirror,
										ProcessorHelper annotationHelper);

	boolean isClassLevelAnnotationProcessor();

	boolean requiresDeveloperCode();

	boolean hasInterfaceClass();

	TypeName getSuperInterface(EntityInfo entityInfo, ProcessorHelper annotationHelper);

	CodeBlock getMethodCode(EntityInfo entityInfo, Element element, List<ParameterSpec> parameterList, AnnotationMirror annotationMirror,
							ProcessorHelper annotationHelper);

	public default String getProcessorName() {
		return getClass().getSimpleName();
	}

	public default List<TypeSpec> getAdditionalClasses(final EntityInfo entityInfo, final ProcessorHelper helper) {
		return new ArrayList<>();
	}
}
