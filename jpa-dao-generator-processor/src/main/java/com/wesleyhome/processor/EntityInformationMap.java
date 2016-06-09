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

import java.util.HashMap;
import java.util.Set;
import com.wesleyhome.annotation.api.EntityInfo;

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
