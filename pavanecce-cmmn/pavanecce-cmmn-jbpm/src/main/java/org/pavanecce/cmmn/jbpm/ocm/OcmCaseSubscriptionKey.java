package org.pavanecce.cmmn.jbpm.ocm;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionKey;

public class OcmCaseSubscriptionKey implements Serializable, CaseSubscriptionKey {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2689025545130505083L;
	private String className;
	private String id;
	private Class<?> entityClass;

	public OcmCaseSubscriptionKey(String path) {
		String[] split = path.substring("/subscriptions/".length()).split("\\$");
		className = split[0];
		id = split[1];
	}

	public OcmCaseSubscriptionKey() {

	}

	public OcmCaseSubscriptionKey(Object object) {
		Member idMember = OcmIdUtil.INSTANCE.findIdMember(object.getClass());
		String idAsString = toIdString(object, idMember);
		this.id = idAsString;
		this.entityClass = OcmIdUtil.INSTANCE.findEntityClass(object.getClass());
		this.className = entityClass.getName();
	}

	private String toIdString(Object object, Member idMember) {
		Object id = OcmIdUtil.INSTANCE.getId(idMember, object);
		String idAsString = null;
		if (id instanceof Number) {
			idAsString = id.toString();
		} else if (id instanceof String) {
			idAsString = (String) id;
		} else if (((AnnotatedElement) idMember).isAnnotationPresent(org.apache.jackrabbit.ocm.mapper.impl.annotation.Field.class)) {
			if (id instanceof Calendar) {
				id = ((Calendar) id).getTime();
			}
			idAsString = new SimpleDateFormat("yyyyMMddHHmmss").format(id);
		} else if (id.getClass().getName().startsWith("java.")) {
			idAsString = id.toString();
		} else {
			Field[] declaredFields = id.getClass().getDeclaredFields();
			StringBuilder sb = new StringBuilder();
			for (Field field : declaredFields) {
				sb.append(field.getName());
				sb.append("=");
				sb.append(toIdString(id, field));
				sb.append(";");
			}
			idAsString = sb.toString();
		}
		return idAsString;
	}

	public String getClassName() {
		return className;
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return className + "$" + id;
	}

	@Override
	public int hashCode() {
		return className.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof OcmCaseSubscriptionKey) {
			OcmCaseSubscriptionKey other = (OcmCaseSubscriptionKey) obj;
			return other.className.equals(className) && other.id.equals(id);
		}
		return false;
	}
}
