/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.annotations;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;


@Target(FIELD)
public @interface ExampleMapping {

	String propertyName();

	Class<?> propertyType();

	String convertMethod();
}
