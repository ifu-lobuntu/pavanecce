package org.pavanecce.cmmn.jbpm.jpa;

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

import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionKey;

@Embeddable
public class JpaCaseSubscriptionKey implements Serializable, CaseSubscriptionKey {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4777697576303219665L;
	private String className;
	private String id;
	@Transient
	private Class<?> entityClass;

	public JpaCaseSubscriptionKey() {

	}

	public JpaCaseSubscriptionKey(Object object) {
		Member idMember = JpaIdUtil.INSTANCE.findIdMember(object.getClass());
		String idAsString = toIdString(object, idMember);
		this.id = idAsString;
		this.entityClass = JpaIdUtil.INSTANCE.findEntityClass(object.getClass());
		this.className = entityClass.getName();
	}

	private String toIdString(Object object, Member idMember) {

		Object id = JpaIdUtil.INSTANCE.getId(idMember, object);
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

	@Override
	public int hashCode() {
		return className.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof JpaCaseSubscriptionKey) {
			JpaCaseSubscriptionKey other = (JpaCaseSubscriptionKey) obj;
			return other.className.equals(className) && other.id.equals(id);
		}
		return false;
	}

	public String getClassName() {
		return className;
	}

	public String getId() {
		return id;
	}
}
