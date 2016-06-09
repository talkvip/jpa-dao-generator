package com.wesleyhome.processors.codegen;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.dao.api.ExampleObject;
import com.wesleyhome.dao.api.Operators;
import com.wesleyhome.dao.api.QueryMetadata;
import com.wesleyhome.processors.annotations.FindByExample;
import com.wesleyhome.processors.annotations.FindByExample.ExampleMapping;
import com.wesleyhome.processors.interfaces.IFindByExample;

public class FindByExampleMethodProcessor implements DaoMethodProcessor {

	@Override
	public Class<? extends Annotation> getAnnotationClassToProcess() {
		return FindByExample.class;
	}

	@Override
	public String getMethodName(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		return "findByExample";
	}

	@Override
	public TypeName getReturnType(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		ClassName returnClassName = ClassName.get(entityInfo.getTypeElement());
		return ParameterizedTypeName.get(ClassName.get(List.class), returnClassName);
	}

	private ClassName getExampleClassName(final EntityInfo entityInfo) {
		return ClassName.get(entityInfo.packageName(), getExampleEntityname(entityInfo));
	}

	private String getExampleEntityname(final EntityInfo entityInfo) {
		return entityInfo.getEntityName() + "Example";
	}

	@Override
	public List<MethodParameter> getParameters(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
		final ProcessorHelper annotationHelper) {
		ClassName exampleClassName = getExampleClassName(entityInfo);
		MethodParameter methodParameter = MethodParameter.of(exampleClassName, "example");
		return Arrays.asList(methodParameter);
	}

	@Override
	public boolean isClassLevelAnnotationProcessor() {
		return true;
	}

	@Override
	public boolean requiresDeveloperCode() {
		return false;
	}

	@Override
	public boolean hasInterfaceClass() {
		return true;
	}

	@Override
	public TypeName getSuperInterface(final EntityInfo entityInfo, final ProcessorHelper annotationHelper) {
		ClassName exampleClassName = getExampleClassName(entityInfo);
		ClassName returnClassName = ClassName.get(entityInfo.getTypeElement());
		return ParameterizedTypeName.get(ClassName.get(IFindByExample.class), exampleClassName, returnClassName);
	}

	@Override
	public CodeBlock getMethodCode(final EntityInfo entityInfo, final Element element, final List<ParameterSpec> parameterList,
		final AnnotationMirror annotationMirror, final ProcessorHelper annotationHelper) {
		/*
		 * CriteriaBuilder cb = this.getCriteriaBuilder();
		 * CriteriaQuery<T> cq = cb.createQuery(T.class);
		 * Root<T> root = cq.from(T.class);
		 * this.applyQueryMetadata(cq, root, example.getQueryMetadata());
		 * return getResultList(cq);
		 */
		String methodCall = entityInfo.isCacheable() ? "getCacheableResultList" : "getResultList";
		return CodeBlock.builder()
			.addStatement("$T cb = this.getCriteriaBuilder()", CriteriaBuilder.class)
			.addStatement("$T<$T> cq = cb.createQuery($T.class)", CriteriaQuery.class, entityInfo.getTypeElement(),
				entityInfo.getTypeElement())
			.addStatement("$T<$T> root = cq.from($T.class)", Root.class, entityInfo.getTypeElement(), entityInfo.getTypeElement())
			.addStatement("$T metadata = example.getQueryMetadata()", QueryMetadata.class)
			.addStatement("this.applyQueryMetadata(cq, root, metadata)")
			.addStatement("return this.$L(cq, metadata)", methodCall)
			.build();
	}

	@Override
	public List<TypeSpec> getAdditionalClasses(final EntityInfo entityInfo, final ProcessorHelper helper) {
		return Arrays.asList(getExampleObjectClass(entityInfo, helper));
	}

	private TypeSpec getExampleObjectClass(final EntityInfo entityInfo, final ProcessorHelper helper) {
		String exampleEntityname = getExampleEntityname(entityInfo);
		String packageName = entityInfo.packageName();
		ClassName returnType = ClassName.get(packageName, exampleEntityname);
		TypeSpec.Builder exampleBuilder = TypeSpec
			.classBuilder(exampleEntityname)
			.addModifiers(Modifier.PUBLIC)
			.superclass(
				ParameterizedTypeName
					.get(
						ClassName.get(ExampleObject.class),
						ClassName.get(entityInfo.packageName(), exampleEntityname)))
			.addMethod(MethodSpec
				.constructorBuilder()
				.addModifiers(Modifier.PRIVATE)
				.addStatement("super()")
				.build())
			.addMethod(MethodSpec
				.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(returnType)
				.addStatement("return new $T()", returnType)
				.build());
		List<VariableElement> mappedProperties = getMappedProperties(entityInfo, helper);
		mappedProperties.forEach(variableElement -> {
			String fieldName = variableElement.getSimpleName().toString();
			String propertyName = fieldName;
			TypeMirror propertyType = variableElement.asType();
			String convertMethod = null;
			AnnotationMirror exampleMapping = helper.getAnnotationMirror(variableElement, ExampleMapping.class);
			if (exampleMapping != null) {
				propertyName = (String) helper.getRealAnnotationValue(exampleMapping, "propertyName");
				propertyType = (TypeMirror) helper.getRealAnnotationValue(exampleMapping, "propertyType");
				convertMethod = (String) helper.getRealAnnotationValue(exampleMapping, "convertMethod");
			}
			ClassName metamodelClassName = ClassName.get(entityInfo.packageName(), entityInfo.getEntityName() + "_");
			ParameterSpec operationParameter = ParameterSpec
				.builder(ClassName.get(Operators.class), "operation", Modifier.FINAL)
				.build();
			ParameterSpec valueParameter = ParameterSpec
				.builder(TypeName.get(propertyType), "value", Modifier.FINAL)
				.build();
			String methodName = "with" + StringUtils.capitalize(propertyName);
			Modifier modifier = Modifier.PUBLIC;
			boolean isBoolean = TypeKind.BOOLEAN.equals(propertyType.getKind());
			if (isBoolean) {
				modifier = Modifier.PRIVATE;
			}
			MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(methodName)
				.addModifiers(modifier)
				.returns(returnType)
				.addParameter(operationParameter)
				.addParameter(valueParameter);
			if (convertMethod == null) {
				methodBuilder
					.addStatement("this.addFilter($N, $T.$L, $N)", operationParameter, metamodelClassName, fieldName, valueParameter);
			} else {
				methodBuilder
					.addStatement("this.addFilter($N, $T.$L, $N.$L())", operationParameter, metamodelClassName, fieldName, valueParameter,
						convertMethod);
			}
			MethodSpec operatorMethod = methodBuilder
				.addStatement("return this.self")
				.build();
			MethodSpec nonOperatorMethod = MethodSpec
				.methodBuilder(methodName)
				.addModifiers(Modifier.PUBLIC)
				.returns(returnType)
				.addParameter(valueParameter)
				.addStatement("return this.$N($T.EQUALS, $N)", operatorMethod, Operators.class, valueParameter)
				.build();
			TypeMirror boxedPropertyType = getBoxedTypeMirror(variableElement, helper);
			exampleBuilder
				.addMethod(operatorMethod)
				.addMethod(nonOperatorMethod);
			if (!isBoolean) {
				methodBuilder = MethodSpec
					.methodBuilder(methodName + "In")
					.addModifiers(Modifier.PUBLIC)
					.returns(returnType);
				if (convertMethod == null) {
					ParameterSpec inValueParameter = ParameterSpec
						.builder(ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(boxedPropertyType)), "values",
							Modifier.FINAL)
						.build();
					methodBuilder
						.addParameter(inValueParameter)
						.addStatement("this.addInFilter($T.$L, $N)", metamodelClassName, fieldName, inValueParameter);
				} else {
					TypeName propertyValueType = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(propertyType));
					ParameterSpec inValueParameter = ParameterSpec
						.builder(propertyValueType, "values",
							Modifier.FINAL)
						.build();
					TypeName convertedValueType = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(boxedPropertyType));
					CodeBlock converterStatement = CodeBlock
						.builder()
						.add("$[$T convertedValue = $N\n", convertedValueType, inValueParameter)
						.add(".stream()\n")
						.add(".map(in -> in.$L())\n", convertMethod)
						.add(".collect($T.toList());\n$]", Collectors.class)
						.build();
					methodBuilder
						.addParameter(inValueParameter)
						.addCode(converterStatement)
						.addStatement("this.addInFilter($T.$L, convertedValue)", metamodelClassName, fieldName);
				}
				MethodSpec inMethod = methodBuilder
					.addStatement("return this.self")
					.build();
				exampleBuilder.addMethod(inMethod);
			}
		});
		return exampleBuilder.build();
	}

	public static TypeMirror getBoxedTypeMirror(final Element element, final ProcessorHelper helper) {
		TypeMirror asType = element.asType();
		if (asType instanceof PrimitiveType && !(asType instanceof DeclaredType)) {
			PrimitiveType primitive = (PrimitiveType) asType;
			TypeElement boxedClass = helper.boxedClass(primitive);
			return boxedClass.asType();
		}
		return asType;
	}

	private List<VariableElement> getMappedProperties(final EntityInfo entityInfo, final ProcessorHelper helper) {
		List<VariableElement> mappedProperties = new ArrayList<>();
		TypeElement entityElement = entityInfo.getTypeElement();
		List<? extends Element> enclosedElements = entityElement.getEnclosedElements();
		for (Element element : enclosedElements) {
			if (element.getKind().isField()) {
				VariableElement variableElement = (VariableElement) element;
				if (isMapped(variableElement, helper)) {
					mappedProperties.add(variableElement);
				}
			}
		}
		EntityInfo superClassInfo = entityInfo.getSuperClassInfo();
		if (superClassInfo != null) {
			mappedProperties.addAll(getMappedProperties(superClassInfo, helper));
		}
		return mappedProperties;
	}

	private boolean isMapped(final VariableElement elm, final ProcessorHelper helper) {
		return helper.getAnnotationMirror(elm, Column.class) != null || helper.getAnnotationMirror(elm, JoinColumn.class) != null;
	}
}
