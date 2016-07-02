/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processor.model;

import com.wesleyhome.annotation.api.ProcessorHelper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import java.util.List;

public class VariableElementTypeField implements TypeField {

	private VariableElement variableElement;

	private ProcessorHelper annotationHelper;

	public VariableElementTypeField(final VariableElement variableElement, final ProcessorHelper annotationHelper) {
		this.variableElement = variableElement;
		this.annotationHelper = annotationHelper;
	}

	@Override
	public String getName() {
		return variableElement.getSimpleName().toString();
	}

	@Override
	public Type getType() {
		return new TypeElementType(annotationHelper.getTypeElement(variableElement.asType()), annotationHelper);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotations() {
		return variableElement.getAnnotationMirrors();
	}
}
