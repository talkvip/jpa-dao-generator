/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processor.model;

import com.squareup.javapoet.TypeName;

import java.util.List;

public interface Type {

	List<TypeField> getFields();

	String getPackageName();

	String getName();

	String getSimpleName();

	TypeName getTypeName();
}
