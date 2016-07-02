/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.codegen;

import com.squareup.javapoet.*;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.dao.api.ExampleObject;
import com.wesleyhome.dao.api.Operators;
import com.wesleyhome.processors.annotations.ExampleMapping;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Justin on 6/24/2016.
 */
public class ExampleClassGenerator {

    public static ClassName getExampleClassName(final EntityInfo entityInfo) {
        return ClassName.get(entityInfo.packageName(), getExampleEntityname(entityInfo));
    }

    public static String getExampleEntityname(final EntityInfo entityInfo) {
        return entityInfo.getEntityName() + "Example";
    }

    public static TypeSpec getExampleObjectClass(final EntityInfo entityInfo, final ProcessorHelper helper) {
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
                propertyName = helper.getRealAnnotationValue(exampleMapping, "propertyName");
                propertyType = helper.getRealAnnotationValue(exampleMapping, "propertyType");
                convertMethod = helper.getRealAnnotationValue(exampleMapping, "convertMethod");
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


    private static List<VariableElement> getMappedProperties(final EntityInfo entityInfo, final ProcessorHelper helper) {
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

    private static boolean isMapped(final VariableElement elm, final ProcessorHelper helper) {
        return helper.getAnnotationMirror(elm, Column.class) != null || helper.getAnnotationMirror(elm, JoinColumn.class) != null;
    }

    private static TypeMirror getBoxedTypeMirror(final Element element, final ProcessorHelper helper) {
        TypeMirror asType = element.asType();
        if (asType instanceof PrimitiveType && !(asType instanceof DeclaredType)) {
            PrimitiveType primitive = (PrimitiveType) asType;
            TypeElement boxedClass = helper.boxedClass(primitive);
            return boxedClass.asType();
        }
        return asType;
    }

}
