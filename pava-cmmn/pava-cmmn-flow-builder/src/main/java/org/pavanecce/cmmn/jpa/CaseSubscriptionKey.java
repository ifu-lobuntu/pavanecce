package org.pavanecce.cmmn.jpa;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Embeddable
public class CaseSubscriptionKey implements Serializable {
	private String className;
	private String id;
	@Transient
	private Class<?> entityClass;
	public CaseSubscriptionKey() {

	}

	public CaseSubscriptionKey(Object object) {
		Member idMember = JpaIdUtil.findIdMember(object.getClass());
		String idAsString = toIdString(object, idMember);
		this.id = idAsString;
		this.entityClass= JpaIdUtil.findEntityClass(object.getClass());
		this.className=entityClass.getName();
	}

	private String toIdString(Object object, Member idMember) {
		Object id = JpaIdUtil.getId(idMember, object);
		String idAsString = null;
		if (id instanceof Number) {
			idAsString = id.toString();
		} else if (id instanceof String) {
			idAsString = (String) id;
		} else if (((AnnotatedElement) idMember).isAnnotationPresent(Temporal.class)) {
			Temporal annotation = ((AnnotatedElement) idMember).getAnnotation(Temporal.class);
			if (id instanceof Calendar) {
				id = ((Calendar) id).getTime();
			}
			if (TemporalType.TIMESTAMP == annotation.value()) {
				idAsString = new SimpleDateFormat("yyyyMMddHHmmss").format(id);
			} else if (TemporalType.TIMESTAMP == annotation.value()) {
				idAsString = new SimpleDateFormat("HHmmss").format(id);
			} else {
				idAsString = new SimpleDateFormat("yyyyMMdd").format(id);
			}
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
}
