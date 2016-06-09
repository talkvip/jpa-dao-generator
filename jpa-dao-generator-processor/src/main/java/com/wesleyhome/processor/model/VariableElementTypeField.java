package com.wesleyhome.processor.model;

import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import com.wesleyhome.annotation.api.ProcessorHelper;

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
