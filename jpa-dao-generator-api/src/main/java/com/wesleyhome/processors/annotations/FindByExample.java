/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.annotations;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Using this annotation will generate the findByFiltered method.
 *
 * <strong>IMPORTANT</strong>
 * This also forces the generation of the deprecated findByExample. The
 * findByExample method will not be generated in the future in favor of the
 * more complete findByFilteredMethod
 * @author Justin Wesley
 */
@Target(TYPE)
public @interface FindByExample {
}
