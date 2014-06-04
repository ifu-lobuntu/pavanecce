package org.pavanecce.cmmn.jbpm.event;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public abstract class AbstractIdUtil<T> {

	public AbstractIdUtil() {
		super();
	}

	@SuppressWarnings("unchecked")
	public T getId(Member idMember, Object object2) {
		Object id = null;
		try {
			Method getter;
			((AccessibleObject) idMember).setAccessible(true);
			if (idMember instanceof Method) {
				getter = (Method) idMember;
				id = getter.invoke(object2);
			} else {
				try {
					getter = object2.getClass().getMethod("get" + Character.toUpperCase(idMember.getName().charAt(0)) + idMember.getName().substring(1));
					getter.setAccessible(true);
					id = getter.invoke(object2);
				} catch (NoSuchMethodException e) {
					id = ((Field) idMember).get(object2);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return (T) id;
	}

	public boolean isEntityObject(Object o) {
		if (o == null) {
			return false;
		}
		if (isEntity(o.getClass())) {
			return true;
		} else if (Object.class == o.getClass().getSuperclass()) {
			return false;
		} else {
			return isEntityObject(o.getClass().getSuperclass());
		}

	}

	public Member findIdMember(Class<?> commonSuperclass) {
		Field[] declaredFields = commonSuperclass.getDeclaredFields();
		for (Field field : declaredFields) {
			if (isId(field)) {
				return field;
			}
		}
		Method[] declaredMethods = commonSuperclass.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (isId(method)) {
				return method;
			}
		}
		if (commonSuperclass.getSuperclass() == Object.class) {
			throw new IllegalArgumentException("Common superclass " + commonSuperclass.getName() + " does not have an id field or property");
		}
		return findIdMember(commonSuperclass.getSuperclass());
	}

	protected abstract boolean isId(AnnotatedElement field);

	public Class<?> findEntityClass(Class<?> commonSuperclass) {
		if (isEntity(commonSuperclass)) {
			return commonSuperclass;
		} else if (Object.class == commonSuperclass.getSuperclass()) {
			throw new IllegalArgumentException("Not an entity: " + commonSuperclass.getName());
		} else {
			return findEntityClass(commonSuperclass.getSuperclass());
		}
	}

	protected abstract boolean isEntity(Class<?> commonSuperclass);

}