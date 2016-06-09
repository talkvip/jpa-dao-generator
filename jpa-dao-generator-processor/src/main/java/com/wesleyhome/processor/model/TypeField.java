package com.wesleyhome.processor.model;

import java.util.List;
import javax.lang.model.element.AnnotationMirror;

public interface TypeField {

	String getName();

	Type getType();

	List<? extends AnnotationMirror> getAnnotations();
}
