/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.annotation.api;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

public interface MethodParameter {

	TypeName getParameterType();

	String getParameterName();

	static MethodParameter of(final VariableElement variableElement) {
		return new MethodParameter() {

			@Override
			public TypeName getParameterType() {
				return TypeName.get(variableElement.asType());
			}

			@Override
			public String getParameterName() {
				return variableElement.getSimpleName().toString();
			}
		};
	}

	static MethodParameter of(final TypeName parameterType, final String parameterName) {
		return new MethodParameter() {

			@Override
			public TypeName getParameterType() {
				return parameterType;
			}

			@Override
			public String getParameterName() {
				// TODO Auto-generated method stub
				return parameterName;
			}
		};
	}
}
