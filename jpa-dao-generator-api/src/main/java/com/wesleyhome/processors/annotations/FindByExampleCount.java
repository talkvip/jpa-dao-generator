/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.annotations;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Using this annotation will generate the findByFiltered method.
 *
 * more complete findByFilteredMethod
 * @author Justin Wesley
 */
@Target(TYPE)
public @interface FindByExampleCount {

}
