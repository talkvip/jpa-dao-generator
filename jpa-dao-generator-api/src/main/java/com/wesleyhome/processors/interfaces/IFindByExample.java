/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.interfaces;

import com.wesleyhome.dao.api.ExampleObject;

import java.util.List;

public interface IFindByExample<E extends ExampleObject<E>, T> {

	List<T> findByExample(E example);
}
