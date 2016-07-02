/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processor.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.wesleyhome.annotation.api.ProcessorHelper;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

public class TypeElementType implements Type {

	private TypeElement typeElement;

	private List<TypeField> fields;

	private ProcessorHelper annotationHelper;

	public TypeElementType(final TypeElement typeElement, final ProcessorHelper annotationHelper) {
		this.typeElement = typeElement;
		this.annotationHelper = annotationHelper;
	}

	@Override
	public String getPackageName() {
		return StringUtils.substringBeforeLast(typeElement.getQualifiedName().toString(), ".");
	}

	public TypeElement getTypeElement() {
		return typeElement;
	}

	@Override
	public List<TypeField> getFields() {
		if (fields == null) {
			fields = new ArrayList<>();
			List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
			for (Element element : enclosedElements) {
				if (element instanceof VariableElement) {
					VariableElement variableElement = (VariableElement) element;
					fields.add(new VariableElementTypeField(variableElement, annotationHelper));
				}
			}
		}
		return fields;
	}

	@Override
	public String getName() {
		return typeElement.getQualifiedName().toString();
	}

	@Override
	public String getSimpleName() {
		return typeElement.getSimpleName().toString();
	}

	@Override
	public TypeName getTypeName() {
		return ClassName.get(typeElement);
	}
}
