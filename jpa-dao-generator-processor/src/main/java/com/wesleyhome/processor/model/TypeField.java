/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processor.model;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;

public interface TypeField {

	String getName();

	Type getType();

	List<? extends AnnotationMirror> getAnnotations();
}
