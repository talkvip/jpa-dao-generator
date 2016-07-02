package com.wesleyhome.processors.interfaces;

import java.io.Serializable;

public interface IExists<K extends Serializable> {

	boolean exists(K key);
}
