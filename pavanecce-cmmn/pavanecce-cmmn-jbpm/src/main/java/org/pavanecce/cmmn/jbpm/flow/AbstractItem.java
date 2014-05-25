package org.pavanecce.cmmn.jbpm.flow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.StateNode;

public class AbstractItem extends StateNode {

	private static final long serialVersionUID = -377075234369815381L;
	private String elementId;

	public AbstractItem() {
		super();
	}

	public String getElementId() {
		return elementId;
	}

	protected void copy(Map<Object, Object> copiedState, Object from, Object to) {
		Class<?> class1 = from.getClass();
		while (class1 != Object.class) {
			if (class1.isInstance(to)) {
				copy(copiedState, from, to, class1);
			}
			class1 = class1.getSuperclass();
		}
	}

	public void setElementId(String elementId) {
		getMetaData().put("UniqueId", elementId);
		this.elementId = elementId;
	}

	private void copy(Map<Object, Object> copiedState, Object from, Object to, Class<?> class1) {
		for (Field field : class1.getDeclaredFields()) {
			if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
				field.setAccessible(true);
				Object toValue = null;
				try {
					if (!isIgnored(to, field)) {
						Object fromFieldValue = field.get(from);
						if (fromFieldValue != null) {
							toValue = copy(copiedState, fromFieldValue);
							field.set(to, toValue);
						}
					}
				} catch (IllegalArgumentException e) {
					if (field.getType().isInstance(toValue)) {
						throw e;
					}
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private boolean isIgnored(Object target, Field field) {
		if (field.getDeclaringClass() == NodeImpl.class && target == this) {
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <V> V copy(Map<Object, Object> copiedState, V fromFieldValue) {
		try {
			if (fromFieldValue instanceof String || fromFieldValue instanceof Number || fromFieldValue instanceof Boolean) {
				return fromFieldValue;
			} else if (copiedState.containsKey(fromFieldValue)) {
				return (V) copiedState.get(fromFieldValue);
			} else if (fromFieldValue instanceof PlanItemDefinition) {
				return fromFieldValue;
			} else if (fromFieldValue instanceof Enum<?>) {
				return fromFieldValue;
			}
			if (fromFieldValue instanceof Collection) {
				Collection fromCollection = (Collection) fromFieldValue;
				Collection toCollection = fromCollection.getClass().newInstance();
				for (Object object : fromCollection) {
					toCollection.add(copy(copiedState, object));
				}
				return (V) toCollection;
			} else if (fromFieldValue instanceof Map) {
				Map fromCollection = (Map) fromFieldValue;
				Map toCollection = fromCollection.getClass().newInstance();
				Set<Map.Entry> entrySet = fromCollection.entrySet();
				for (Map.Entry object : entrySet) {
					toCollection.put(copy(copiedState, object.getKey()), copy(copiedState, object.getValue()));
				}
				return (V) toCollection;
			} else {
				Object newInstance = fromFieldValue.getClass().newInstance();
				copiedState.put(fromFieldValue, newInstance);
				copy(copiedState, fromFieldValue, newInstance);
				return (V) newInstance;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}