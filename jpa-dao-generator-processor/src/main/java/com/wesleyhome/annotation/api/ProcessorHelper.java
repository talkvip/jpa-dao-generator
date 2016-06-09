package com.wesleyhome.annotation.api;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

public interface ProcessorHelper {

	TypeElement getTypeElement(final TypeMirror asType);

	List<TypeElement> getTypeElements(final AnnotationMirror mirror, final String key);

	TypeElement getTypeElement(final AnnotationMirror mirror, final String key);

	List<AnnotationValue> getAnnotationValueList(final AnnotationMirror annotationMirror, final String key);

	List<VariableElement> getAnnotatedFields(final TypeElement typeElement, final Class<? extends Annotation> annotation);

	AnnotationValue getAnnotationValue(final AnnotationMirror annotationMirror, final String key);

	AnnotationMirror getAnnotationMirror(final Element typeElement, final String clazzName);

	AnnotationMirror getAnnotationMirror(final Element typeElement, final Class<? extends Annotation> annotationClass);

	<V> V getRealAnnotationValue(final AnnotationMirror mirror, final String keyName);

	Object getAnnotationValue(final Element element, final String clazzName, final String keyName);

	Object getAnnotationValue(final Element element, final Class<? extends Annotation> annotationClass, final String keyName);

	void printMessage(final Kind kind, final CharSequence msg, final Element e, final AnnotationMirror a, final AnnotationValue v);

	void printMessage(final Kind kind, final CharSequence msg, final Element e, final AnnotationMirror a);

	void printMessage(final Kind kind, final CharSequence msg, final Element e);

	void printMessage(final Kind kind, final CharSequence msg);

	TypeElement boxedClass(PrimitiveType primitive);
}
