/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processors.interfaces;

import java.io.Serializable;

public interface IFindByPK<K extends Serializable, V> {

	V findByPK(K key);
}
