package com.wesleyhome.annotation.api;

import javax.lang.model.element.VariableElement;
import com.squareup.javapoet.TypeName;

public interface MethodParameter {

	TypeName getParameterType();

	String getParameterName();

	public static MethodParameter of(final VariableElement variableElement) {
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

	public static MethodParameter of(final TypeName parameterType, final String parameterName) {
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
