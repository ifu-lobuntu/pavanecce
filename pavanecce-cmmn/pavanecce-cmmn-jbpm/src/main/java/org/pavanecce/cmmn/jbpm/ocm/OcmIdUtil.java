package org.pavanecce.cmmn.jbpm.ocm;

import java.lang.reflect.AnnotatedElement;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.cmmn.jbpm.event.AbstractIdUtil;

public class OcmIdUtil extends AbstractIdUtil<String> {
	public static OcmIdUtil INSTANCE = new OcmIdUtil();

	@Override
	protected boolean isId(AnnotatedElement field) {
		boolean isId = false;
		if (field.isAnnotationPresent(org.apache.jackrabbit.ocm.mapper.impl.annotation.Field.class)) {
			org.apache.jackrabbit.ocm.mapper.impl.annotation.Field ann = field.getAnnotation(org.apache.jackrabbit.ocm.mapper.impl.annotation.Field.class);
			if (ann.id() || ann.uuid()) {
				isId = true;
				;
			}
		}
		return isId;
	}

	@Override
	protected boolean isEntity(Class<?> commonSuperclass) {
		return commonSuperclass.isAnnotationPresent(Node.class);
	}

}
