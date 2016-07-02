/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processor;

import com.wesleyhome.annotation.api.ProcessorHelper;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The <code>AnnotationHelper</code> class is a
 *
 * @author
 * @since
 */
class ProcessorHelperImpl implements ProcessorHelper {

	private Elements elements;
	private Types types;
	private Messager messager;

	ProcessorHelperImpl(final Elements elements, final Types types, final Messager messager) {
		this.elements = elements;
		this.types = types;
		this.messager = messager;
	}

	@Override
	public void printMessage(final Kind kind, final CharSequence msg) {
		messager.printMessage(kind, msg);
	}

	@Override
	public void printMessage(final Kind kind, final CharSequence msg, final Element e) {
		messager.printMessage(kind, msg, e);
	}

	@Override
	public void printMessage(final Kind kind, final CharSequence msg, final Element e, final AnnotationMirror a) {
		messager.printMessage(kind, msg, e, a);
	}

	@Override
	public void printMessage(final Kind kind, final CharSequence msg, final Element e, final AnnotationMirror a, final AnnotationValue v) {
		messager.printMessage(kind, msg, e, a, v);
	}

	@Override
	public Object getAnnotationValue(final Element element, final Class<? extends Annotation> annotationClass, final String keyName) {
		return getAnnotationValue(element, annotationClass.getName(), keyName);
	}

	@Override
	public Object getAnnotationValue(final Element element, final String clazzName, final String keyName) {
		AnnotationMirror mirror = getAnnotationMirror(element, clazzName);
		return getRealAnnotationValue(mirror, keyName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getRealAnnotationValue(final AnnotationMirror mirror, final String keyName) {
		if (mirror != null) {
			AnnotationValue annotationValue = getAnnotationValue(mirror, keyName);
			return (V) (annotationValue == null ? null : annotationValue.getValue());
		}
		return null;
	}

	@Override
	public AnnotationMirror getAnnotationMirror(final Element typeElement, final Class<? extends Annotation> annotationClass) {
		return getAnnotationMirror(typeElement, annotationClass.getName());
	}

	@Override
	public AnnotationMirror getAnnotationMirror(final Element typeElement, final String clazzName) {
		for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
			String newClassName = clazzName.replaceAll("\\$", ".");
			if (m.getAnnotationType().toString().equals(newClassName)) {
				return m;
			}
		}
		return null;
	}

	@Override
	public AnnotationValue getAnnotationValue(final AnnotationMirror annotationMirror, final String key) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
			if (entry.getKey().getSimpleName().toString().equals(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public List<VariableElement> getAnnotatedFields(final TypeElement typeElement, final Class<? extends Annotation> annotation) {
		List<VariableElement> list = new ArrayList<>();
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
		for (Element element : enclosedElements) {
			if (element.getKind().isField()) {
				VariableElement variableElement = (VariableElement) element;
				List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
				for (AnnotationMirror annotationMirror : annotationMirrors) {
					String annotationType = annotationMirror.getAnnotationType().toString();
					if (annotation.getName().equals(annotationType)) {
						list.add(variableElement);
					}
				}
			}
		}
		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AnnotationValue> getAnnotationValueList(final AnnotationMirror annotationMirror, final String key) {
		return (List<AnnotationValue>) getAnnotationValue(annotationMirror, key).getValue();
	}

	@Override
	public TypeElement getTypeElement(final AnnotationMirror mirror, final String key) {
		AnnotationValue annotationValue = getAnnotationValue(mirror, key);
		if (annotationValue == null) {
			return null;
		}
		return elements.getTypeElement(annotationValue.getValue().toString());
	}

	@Override
	public List<TypeElement> getTypeElements(final AnnotationMirror mirror, final String key) {
		AnnotationValue annotationValue = getAnnotationValue(mirror, key);
		List<TypeElement> list = new ArrayList<TypeElement>();
		if (annotationValue != null) {
			@SuppressWarnings("unchecked")
			List<AnnotationValue> annotationValueList = (List<AnnotationValue>) annotationValue.getValue();
			for (AnnotationValue value : annotationValueList) {
				TypeElement typeElement = elements.getTypeElement(value.getValue().toString());
				list.add(typeElement);
			}
		}
		return list;
	}

	@Override
	public TypeElement getTypeElement(final TypeMirror asType) {
		return (TypeElement) types.asElement(asType);
	}

	@Override
	public TypeElement boxedClass(final PrimitiveType primitive) {
		return types.boxedClass(primitive);
	}
}
