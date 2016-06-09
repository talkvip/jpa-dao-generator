package com.wesleyhome.processors.interfaces;

import java.util.List;
import com.wesleyhome.dao.api.ExampleObject;

public interface IFindByExample<E extends ExampleObject<E>, T> {

	List<T> findByExample(E example);
}
