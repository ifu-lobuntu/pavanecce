package org.pavanecce.cmmn.jbpm.jpa;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.persistence.Query;

import org.drools.core.common.DroolsObjectInputStream;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.kie.api.runtime.Environment;
import org.pavanecce.common.jpa.JpaObjectPersistence;

public class JpaCollectionPlaceHolderResolverStrategy extends JPAPlaceholderResolverStrategy {
	private Environment env;

	public JpaCollectionPlaceHolderResolverStrategy(Environment env) {
		super(env);
		this.env = env;
	}

	@Override
	public boolean accept(Object object) {
		if (object instanceof Collection) {
			Collection<?> coll = (Collection<?>) object;
			if (coll.size() == 0) {
				return true;
			} else {
				Class<?> sc = findCommonSuperclass(coll);
				if (sc == null) {
					return false;
				} else {
					try {
						JpaIdUtil.INSTANCE.findIdMember(sc);
						return true;
					} catch (Exception e) {
						return false;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void write(ObjectOutputStream os, Object collection) throws IOException {
		if (collection instanceof Stack) {
			os.writeUTF(Stack.class.getName());
		} else if (collection instanceof List) {
			os.writeUTF(ArrayList.class.getName());
		} else if (collection instanceof Set) {
			os.writeUTF(HashSet.class.getName());
		} else {
			throw new IllegalArgumentException();
		}
		Collection<?> coll = (Collection<?>) collection;
		os.writeInt(coll.size());
		if (coll.size() > 0) {
			Class<?> commonSuperclass = findCommonSuperclass(coll);
			os.writeUTF(commonSuperclass.getName());
			Member idMember = JpaIdUtil.INSTANCE.findIdMember(commonSuperclass);
			((AccessibleObject) idMember).setAccessible(true);
			String idName = idMember.getName();
			if (idMember instanceof Method) {
				idName = Introspector.decapitalize(idName.substring(3));
			}
			os.writeUTF(idName);
			for (Object object2 : coll) {
				Object id = JpaIdUtil.INSTANCE.getId(idMember, object2);
				if (id == null) {
					throw new IllegalStateException("Id must be set before being stored in a process: " + commonSuperclass.getName() + "." + idMember.getName());
				}
				os.writeObject(id);
			}
		}
	}

	public Class<?> findCommonSuperclass(Collection<?> c) {
		Iterator<?> iterator = c.iterator();
		Object next = iterator.next();
		Class<?> result = next.getClass();
		for (Object object : c) {
			while (!result.isInstance(object)) {
				result = result.getSuperclass();
			}
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object read(ObjectInputStream is) throws IOException, ClassNotFoundException {
		String canonicalName = is.readUTF();
		Collection<Object> coll = null;
		try {
			coll = (Collection<Object>) Class.forName(canonicalName).newInstance();
		} catch (InstantiationException e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
		int size = is.readInt();
		if (size > 0) {
			List<?> resultList = readCollection(is, size);
			coll.addAll(resultList);
		}
		return coll;
	}

	@SuppressWarnings("unused")
	private List<?> readCollectionOptimized(ObjectInputStream is, int size) throws ClassNotFoundException, IOException {
		Class<?> superClass = Class.forName(is.readUTF());
		String idName = is.readUTF();
		Collection<Object> ids = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			ids.add(is.readObject());
		}
		JpaObjectPersistence jop = (JpaObjectPersistence) env.get(JpaObjectPersistence.ENV_NAME);
		Query q = jop.getEntityManager().createQuery("select o from " + superClass.getName() + " o where o." + idName + " in (:ids)");
		q.setParameter("ids", ids);
		return q.getResultList();
	}

	private List<?> readCollection(ObjectInputStream is, int size) throws ClassNotFoundException, IOException {
		Class<?> superClass = Class.forName(is.readUTF());
		@SuppressWarnings("unused")
		String idName = is.readUTF();
		List<Object> result = new ArrayList<Object>();
		JpaObjectPersistence jop = (JpaObjectPersistence) env.get(JpaObjectPersistence.ENV_NAME);
		for (int i = 0; i < size; i++) {
			result.add(jop.find(superClass, is.readObject()));
		}
		return result;
	}

	@Override
	public byte[] marshal(Context context, ObjectOutputStream os, Object object) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(buff);
		write(oos, object);
		oos.close();
		return buff.toByteArray();
	}

	@Override
	public Object unmarshal(Context context, ObjectInputStream ois, byte[] object, ClassLoader classloader) throws IOException, ClassNotFoundException {
		DroolsObjectInputStream is = new DroolsObjectInputStream(new ByteArrayInputStream(object), classloader);
		return read(is);
	}

	@Override
	public Context createContext() {
		// no need for context
		return null;
	}

}
