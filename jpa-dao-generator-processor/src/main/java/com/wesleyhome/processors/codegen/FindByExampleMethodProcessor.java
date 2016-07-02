package com.wesleyhome.processors.codegen;

import com.squareup.javapoet.*;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.dao.api.QueryMetadata;
import com.wesleyhome.processors.annotations.FindByExample;
import com.wesleyhome.processors.interfaces.IFindByExample;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

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


    @Override
    public List<MethodParameter> getParameters(final EntityInfo entityInfo, final Element element, final AnnotationMirror annotationMirror,
                                               final ProcessorHelper annotationHelper) {
        ClassName exampleClassName = ExampleClassGenerator.getExampleClassName(entityInfo);
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
        ClassName exampleClassName = ExampleClassGenerator.getExampleClassName(entityInfo);
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
        return Arrays.asList(ExampleClassGenerator.getExampleObjectClass(entityInfo, helper));
    }
}
