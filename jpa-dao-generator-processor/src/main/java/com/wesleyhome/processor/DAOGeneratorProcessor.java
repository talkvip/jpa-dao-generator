/*
 * Copyright 2014 Justin Wesley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wesleyhome.processor;

import static com.wesleyhome.processor.DAOGeneratorProcessor.DAO_NAMED_KEY;
import static com.wesleyhome.processor.DAOGeneratorProcessor.ENTITY_MANANGER_INJECT_KEY;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.tools.Diagnostic.Kind;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wesleyhome.annotation.api.DaoMethodProcessor;
import com.wesleyhome.annotation.api.EntityInfo;
import com.wesleyhome.annotation.api.MethodParameter;
import com.wesleyhome.annotation.api.ProcessorHelper;
import com.wesleyhome.dao.api.BaseDAO;
import com.wesleyhome.dao.api.DAO;
import com.wesleyhome.dao.api.Delegate;
import com.wesleyhome.processor.model.Type;
import com.wesleyhome.processor.model.TypeElementType;
import com.wesleyhome.processors.codegen.FindAllMethodProcessor;
import com.wesleyhome.processors.codegen.FindByExampleMethodProcessor;
import com.wesleyhome.processors.codegen.FindByPKMethodProcessor;

/**
 * The <code>DAOGeneratorProcessor</code> class is a
 *
 * @author
 * @since
 */
@SupportedAnnotationTypes({
	"javax.persistence.Entity", "javax.persistence.MappedSuperclass"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({
	DAO_NAMED_KEY, ENTITY_MANANGER_INJECT_KEY
})
@AutoService(Processor.class)
public class DAOGeneratorProcessor extends AbstractProcessor {

	public static final String DAO_NAMED_KEY = "dao.generator.named";
	public static final String DAO_STATELESS_KEY = "dao.generator.stateless";
	public static final String DAO_LOCAL_KEY = "dao.generator.local";
	public static final String ENTITY_MANANGER_INJECT_KEY = "dao.generator.inject";

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.
	 * processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(final ProcessingEnvironment _processingEnv) {
		super.init(_processingEnv);
		Environment.setEnvironment(processingEnv);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 * javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		Messager messager = processingEnv.getMessager();
		try {
			if (annotations.isEmpty()) {
				return false;
			}
			Types typeUtils = processingEnv.getTypeUtils();
			Elements elementUtils = processingEnv.getElementUtils();
			ProcessorHelper annotationHelper = new ProcessorHelperImpl(elementUtils, typeUtils, messager);
			Set<? extends Element> mappedSuperClassElements = roundEnv.getElementsAnnotatedWith(MappedSuperclass.class);
			final EntityInformationMap mappedSuperClassInformation = getEntityInformation(mappedSuperClassElements, annotationHelper);
			mappedSuperClassInformation.apply(mappedSuperClassInformation);
			Set<? extends Element> entityClassElements = roundEnv.getElementsAnnotatedWith(Entity.class);
			final EntityInformationMap entityInfoMap = getEntityInformation(entityClassElements, annotationHelper);
			entityInfoMap.apply(entityInfoMap);
			String dateTimeString = new SimpleDateFormat("MM/dd/yyyy hh:mm").format(new Date());
			String generatorClassName = getClass().getName();
			String generatorComments = "Generated by DAO Generator";
			messager.printMessage(Kind.NOTE, String.format("Processing entities with %s", generatorClassName));
			List<DaoMethodProcessor> methodProcessors = getMethodProcessors();
			entityInfoMap
				.values()
				.stream()
				.map(i -> {
					List<JavaFile> fileList = getFileList(i, annotationHelper, dateTimeString, generatorClassName, generatorComments,
						methodProcessors);
					return fileList;
				})
				.flatMap(l -> l.stream())
				.collect(Collectors.toList())
				.stream()
				.forEach(file -> {
					try {
						file.writeTo(processingEnv.getFiler());
					} catch (Exception e) {
						messager.printMessage(Kind.ERROR, e.getMessage());
						e.printStackTrace();
					}
				});
		} catch (Exception e) {
			messager.printMessage(Kind.ERROR, String.format("DAOGenerator: Error Processing %s", e.getStackTrace().toString()));
			throw new RuntimeException(e);
		}
		return false;
	}

	private List<DaoMethodProcessor> getMethodProcessors() {
		return Arrays.asList(
			new FindByPKMethodProcessor(),
			new FindAllMethodProcessor(),
			new FindByExampleMethodProcessor());
	}

	private List<JavaFile> getFileList(final EntityInfo entityInfo, final ProcessorHelper annotationHelper, final String dateTimeString,
		final String generatorClassName, final String generatorComments, final List<DaoMethodProcessor> methodProcessors) {
		List<JavaFile> fileList = new ArrayList<>();
		long count = methodProcessors.stream().count();
		if (count > 0) {
			TypeElement typeElement = entityInfo.getTypeElement();
			VariableElement idElement = entityInfo.getIdElement();
			Type type = new TypeElementType(typeElement, annotationHelper);
			String packageName = type.getPackageName();
			String entityName = entityInfo.getEntityName();
			String daoInterfaceName = String.format("%sDao", entityName);
			String abstractClassName = String.format("Abstract%s", daoInterfaceName);
			String delegateClassName = String.format("Delegate%s", daoInterfaceName);
			String concreteClassName = String.format("Default%s", daoInterfaceName);
			DateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			AnnotationSpec generatedAnnotation = AnnotationSpec
				.builder(ClassName.get(Generated.class))
				.addMember("value", "$S", getClass().getName())
				.addMember("date", "$S", simpleDateFormat.format(new Date()))
				.build();
			AnnotationSpec localAnnotation = AnnotationSpec
				.builder(ClassName.get("javax.ejb", "Local"))
				.addMember("value", "$L.class", daoInterfaceName)
				.build();
			TypeSpec.Builder interfaceBuilder = TypeSpec
				.interfaceBuilder(daoInterfaceName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(ClassName.get(DAO.class))
				.addAnnotation(localAnnotation)
				.addAnnotation(generatedAnnotation);
			FieldSpec entityManagerField = FieldSpec
				.builder(ClassName.get(EntityManager.class), "entityManager", Modifier.PRIVATE)
				.addAnnotation(ClassName.bestGuess("javax.inject.Inject")).build();
			FieldSpec cacheRegionField = FieldSpec
				.builder(ClassName.get(String.class), "CACHE_REGION_NAME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
				.initializer("$S", entityName).build();
			TypeSpec.Builder abstractClassBuilder = TypeSpec.classBuilder(abstractClassName)
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addMethod(MethodSpec
					.constructorBuilder()
					.addModifiers(Modifier.PROTECTED)
					.build())
				.addAnnotation(generatedAnnotation)
				.addField(entityManagerField)
				.addField(cacheRegionField)
				.addMethod(MethodSpec
					.methodBuilder("getCacheRegion")
					.addModifiers(Modifier.PUBLIC)
					.returns(ClassName.get(String.class))
					.addStatement("return $N", cacheRegionField)
					.build())
				.addMethod(MethodSpec
					.methodBuilder("getEntityManager")
					.addModifiers(Modifier.PUBLIC)
					.returns(ClassName.get(EntityManager.class))
					.addStatement("return $N", entityManagerField)
					.build())
				.superclass(ParameterizedTypeName.get(ClassName.get(BaseDAO.class), type.getTypeName()));
			TypeSpec.Builder delegateClassBuilder = TypeSpec
				.classBuilder(delegateClassName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(generatedAnnotation);
			String delegateFieldName = "delegate";
			FieldSpec delegateField = FieldSpec
				.builder(ClassName.get(packageName, daoInterfaceName), delegateFieldName, Modifier.PRIVATE)
				.addAnnotation(Delegate.class)
				.addAnnotation(ClassName.bestGuess("javax.inject.Inject"))
				.build();
			TypeSpec.Builder concreteClassBuilder = TypeSpec
				.classBuilder(concreteClassName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(generatedAnnotation)
				.addAnnotation(ClassName.get("javax.ejb", "Stateless"))
				.addAnnotation(ClassName.get("javax.inject", "Named"))
				.addField(delegateField);
			boolean requiresDeveloper = methodProcessors
				.stream()
				.map(processor -> processor.requiresDeveloperCode())
				.reduce((t, u) -> t.booleanValue() && u.booleanValue())
				.get()
				.booleanValue();
			methodProcessors
				.stream()
				.forEach(processor -> {
					Class<? extends Annotation> annotationClass = processor.getAnnotationClassToProcess();
					boolean classLevel = processor.isClassLevelAnnotationProcessor();
					List<? extends Element> elementlist = null;
					if (classLevel) {
						elementlist = Arrays.asList(typeElement);
					} else {
						elementlist = annotationHelper.getAnnotatedFields(typeElement, annotationClass);
					}
					elementlist
						.stream()
						.forEach(element -> {
							AnnotationMirror annotationMirror = annotationHelper.getAnnotationMirror(element, annotationClass);
							if (annotationMirror != null) {
								processor.getAdditionalClasses(entityInfo, annotationHelper)
									.forEach(t -> addJavaFile(fileList, packageName, t));
								annotationHelper.printMessage(Kind.NOTE,
									String.format("Processing %s with %s", entityName, processor.getProcessorName()));
								boolean hasExtensionInterface = processor.hasInterfaceClass();
								if (hasExtensionInterface) {
									TypeName superInterface = processor.getSuperInterface(entityInfo, annotationHelper);
									interfaceBuilder.addSuperinterface(superInterface);
								}
								String methodName = processor.getMethodName(entityInfo, element, annotationMirror, annotationHelper);
								TypeName returnType = processor.getReturnType(entityInfo, element, annotationMirror, annotationHelper);
								List<MethodParameter> methodParameters = processor.getParameters(entityInfo, element, annotationMirror,
									annotationHelper);
								List<ParameterSpec> parameterList = methodParameters.stream()
									.map(m -> ParameterSpec
										.builder(m.getParameterType(), m.getParameterName(), Modifier.FINAL)
										.build())
									.collect(Collectors.toList());
								if (!processor.hasInterfaceClass()) {
									interfaceBuilder
										.addMethod(getMethodBuilder(methodName, returnType, parameterList)
											.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
											.build());
								}
								addToConcreteClass(delegateFieldName, concreteClassBuilder, methodName, returnType, parameterList,
									getMethodBuilder(methodName, returnType, parameterList));
								if (!requiresDeveloper) {
									CodeBlock methodCode = processor.getMethodCode(entityInfo, idElement, parameterList, annotationMirror,
										annotationHelper);
									abstractClassBuilder
										.addMethod(getMethodBuilder(methodName, returnType, parameterList)
											.addModifiers(Modifier.PUBLIC)
											.addCode(methodCode)
											.build());
								}
							}
						});
				});
			TypeSpec interfaceTypeSpec = interfaceBuilder
				.addSuperinterface(ClassName.get(Serializable.class))
				.build();
			JavaFile interfaceFile = addJavaFile(fileList, packageName, interfaceTypeSpec);
			ClassName interfaceClassName = getClassName(interfaceFile);
			FieldSpec serialVersionUID = FieldSpec
				.builder(TypeName.LONG, "serialVersionUID", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
				.initializer("1L").build();
			TypeSpec abstractClassTypeSpec = abstractClassBuilder
				.addSuperinterface(interfaceClassName)
				.addField(serialVersionUID)
				.build();
			JavaFile abstractClassFile = addJavaFile(fileList, packageName, abstractClassTypeSpec);
			TypeSpec concreteClassTypeSpec = concreteClassBuilder
				.addSuperinterface(interfaceClassName)
				.addField(serialVersionUID)
				.build();
			addJavaFile(fileList, packageName, concreteClassTypeSpec);
			if (!requiresDeveloper) {
				TypeSpec delegateClassTypeSpec = delegateClassBuilder
					.superclass(getClassName(abstractClassFile))
					.addField(serialVersionUID)
					.addAnnotation(Delegate.class)
					.build();
				addJavaFile(fileList, packageName, delegateClassTypeSpec);
			}
		}
		return fileList;
	}

	private MethodSpec.Builder getMethodBuilder(final String methodName, final TypeName returnType,
		final List<ParameterSpec> parameterList) {
		return MethodSpec
			.methodBuilder(methodName)
			.returns(returnType)
			.addParameters(parameterList)
			.addAnnotation(Override.class);
	}

	private JavaFile addJavaFile(final List<JavaFile> fileList, final String packageName, final TypeSpec interfaceTypeSpec) {
		JavaFile interfaceFile = getJavaFile(packageName, interfaceTypeSpec);
		fileList.add(interfaceFile);
		return interfaceFile;
	}

	private JavaFile getJavaFile(final String packageName, final TypeSpec interfaceTypeSpec) {
		return JavaFile
			.builder(packageName, interfaceTypeSpec)
			.build();
	}

	private ClassName getClassName(final JavaFile javaFile) {
		return ClassName.get(javaFile.packageName, javaFile.typeSpec.name);
	}

	private void addToConcreteClass(final String delegateFieldName, final com.squareup.javapoet.TypeSpec.Builder concreteClassBuilder,
		final String methodName, final TypeName returnType, final List<ParameterSpec> parameterList,
		final MethodSpec.Builder methodBuilder) {
		String returnString = !TypeName.VOID.equals(returnType) ? "return " : "";
		String delegateCall = String.format("%sthis.%s.%s(%s)", returnString, delegateFieldName, methodName,
			parameterList
				.stream()
				.map(spec -> spec.name)
				.collect(Collectors.joining(", ")));
		concreteClassBuilder
			.addMethod(methodBuilder
				.addModifiers(Modifier.PUBLIC)
				.addStatement(delegateCall)
				.build());
	}

	private EntityInformationMap getEntityInformation(final Set<? extends Element> entityElements,
		final ProcessorHelper annotationHelper) {
		EntityInformationMap map = new EntityInformationMap();
		EntityInfoProcessor processor = new EntityInfoProcessor(annotationHelper);
		for (Element element : entityElements) {
			if (element instanceof TypeElement) {
				TypeElement entityElement = (TypeElement) element;
				String fullTypeName = entityElement.asType().toString();
				int indexOf = fullTypeName.indexOf("<");
				if (indexOf >= 0) {
					fullTypeName = fullTypeName.substring(0, indexOf);
				}
				EntityInfoImpl entityInfo = processor.getEntityInfo(entityElement);
				map.put(fullTypeName, entityInfo);
			}
		}
		return map;
	}
}