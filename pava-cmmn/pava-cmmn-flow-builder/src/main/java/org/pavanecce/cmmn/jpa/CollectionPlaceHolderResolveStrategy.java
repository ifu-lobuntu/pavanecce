package org.pavanecce.cmmn.jpa;

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
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.drools.core.common.DroolsObjectInputStream;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionPlaceHolderResolveStrategy extends JPAPlaceholderResolverStrategy {
	private static Logger log = LoggerFactory.getLogger(CollectionPlaceHolderResolveStrategy.class);
	private Environment env;

	public CollectionPlaceHolderResolveStrategy(Environment env) {
		super(env);
		this.env=env;
	}

	public boolean accept(Object object) {
		return (object instanceof Collection);
	}

	public void write(ObjectOutputStream os, Object collection) throws IOException {
		if (collection instanceof List) {
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
			Member idMember = JpaIdUtil.findIdMember(commonSuperclass);
			((AccessibleObject) idMember).setAccessible(true);
			String idName = idMember.getName();
			if(idMember instanceof Method){
				idName=Introspector.decapitalize(idName.substring(3));
			}
			os.writeUTF(idName);
			for (Object object2 : coll) {
				Object id = JpaIdUtil.getId(idMember, object2);
				if(id==null){
					throw new IllegalStateException("Id must be set before being stored in a process: " + commonSuperclass.getName() +"."+ idMember.getName());
				}
				os.writeObject(id);
			}
		}
	}

	Class<?> findCommonSuperclass(Collection<?> c) {
		Class<?> result = c.iterator().next().getClass();
		for (Object object : c) {
			while (!result.isInstance(object)) {
				result = result.getSuperclass();
			}
		}
		return result;
	}

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
			Class<?> superClass = Class.forName(is.readUTF());
			String idName=is.readUTF();
			Collection<Object> ids = new ArrayList<Object>();
			for (int i = 0; i < size; i++) {
				ids.add(is.readObject());
			}
			EntityManagerFactory emf = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
			EntityManager em = emf.createEntityManager();
			Query q = em.createQuery("select o from " + superClass.getName() + " o where o." + idName +" in (:ids)");
			q.setParameter("ids", ids);
			coll.addAll(q.getResultList());
		}
		return coll;
	}

	public byte[] marshal(Context context, ObjectOutputStream os, Object object) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(buff);
		write(oos, object);
		oos.close();
		return buff.toByteArray();
	}

	public Object unmarshal(Context context, ObjectInputStream ois, byte[] object, ClassLoader classloader) throws IOException, ClassNotFoundException {
		DroolsObjectInputStream is = new DroolsObjectInputStream(new ByteArrayInputStream(object), classloader);
		return read(is);
	}
	public Context createContext() {
		// no need for context
		return null;
	}

}
