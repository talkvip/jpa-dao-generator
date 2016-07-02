/*
 * Copyright (c) 2016. Justin Wesley
 */

package com.wesleyhome.processor;

import com.wesleyhome.annotation.api.EntityInfo;

import java.util.HashMap;
import java.util.Set;

public class EntityInformationMap extends HashMap<String, EntityInfoImpl> {
	private static final long serialVersionUID = 5509824824260569783L;

	public void apply(final EntityInformationMap classInformation) {
		Set<String> keySet2 = classInformation.keySet();
		for (String typeMirror : keySet2) {
			if (!containsKey(typeMirror)) {
				put(typeMirror, classInformation.get(typeMirror));
			}
		}
		Set<String> keys = keySet();
		for (String typeMirror : keys) {
			EntityInfo entityInfo = get(typeMirror);
			String superclass = entityInfo.getTypeElement().getSuperclass().toString();
			int indexOf = superclass.indexOf("<");
			if (indexOf >= 0) {
				superclass = superclass.substring(0, indexOf);
			}
			if (containsKey(superclass)) {
				EntityInfoImpl superInfo = get(superclass);
				if (superInfo == null) {
					System.out.println("WHAT???");
				} else {
					entityInfo.applySuperclass(superInfo);
				}
			}
		}
	}
}
