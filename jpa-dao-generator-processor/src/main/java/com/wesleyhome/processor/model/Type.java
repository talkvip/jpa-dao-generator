package com.wesleyhome.processor.model;

import java.util.List;
import com.squareup.javapoet.TypeName;

public interface Type {

	public List<TypeField> getFields();

	public String getPackageName();

	public String getName();

	public String getSimpleName();

	public TypeName getTypeName();
}
