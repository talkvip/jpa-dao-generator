/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.annotation.api;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * The <code>SubPackage</code> class is a
 *
 * @author
 * @since
 */
@Target(TYPE)
public @interface Scope {
	ScopeType value() default ScopeType.NONE;
}
