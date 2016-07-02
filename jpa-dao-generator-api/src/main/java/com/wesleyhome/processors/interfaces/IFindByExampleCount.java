/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.interfaces;

import com.wesleyhome.dao.api.ExampleObject;

public interface IFindByExampleCount<E extends ExampleObject<E>> {

	Long getCount(E example);
}
