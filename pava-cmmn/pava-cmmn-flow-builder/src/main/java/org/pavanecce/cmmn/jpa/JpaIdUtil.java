package org.pavanecce.cmmn.jpa;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;

public class JpaIdUtil {

	public static Object getId(Member idMember, Object object2) {
		Object id=null;
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
		return id;
	}

	public static Member findIdMember(Class<?> commonSuperclass) {
		Field[] declaredFields = commonSuperclass.getDeclaredFields();
		for (Field field : declaredFields) {
			if(field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)){
				return field;
			}
		}
		Method[] declaredMethods = commonSuperclass.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if(method.isAnnotationPresent(Id.class) ||method.isAnnotationPresent(EmbeddedId.class)){
				return method;
			}
		}
		if(commonSuperclass.getSuperclass()==Object.class){
			throw new IllegalArgumentException("Common superclass " + commonSuperclass.getName() + " does not have an id field or property");
		}
		return findIdMember(commonSuperclass.getSuperclass());
	}
	public static Class<?> findEntityClass(Class<?> commonSuperclass) {
		if(commonSuperclass.isAnnotationPresent(Entity.class)){
			return commonSuperclass;
		}else if(Object.class==commonSuperclass.getSuperclass()){
			throw new IllegalArgumentException("Not an entity: " + commonSuperclass.getName());
		}else{
			return findEntityClass(commonSuperclass.getSuperclass());
		}
	}

}
