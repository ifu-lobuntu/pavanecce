package org.pavanecce.cmmn.jbpm.ocm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.drools.core.common.DroolsObjectInputStream;
import org.kie.api.runtime.Environment;
import org.pavanecce.cmmn.jbpm.jpa.JpaCollectionPlaceHolderResolverStrategy;
import org.pavanecce.common.ocm.ObjectContentManagerFactory;

public class OcmCollectionPlaceHolderResolveStrategy extends JpaCollectionPlaceHolderResolverStrategy {
	private Environment env;

	public OcmCollectionPlaceHolderResolveStrategy(Environment env) {
		super(env);
		this.env = env;
	}

	@Override
	public boolean accept(Object object) {
		if (object instanceof Collection) {
			Collection<?> c = (Collection<?>) object;
			if (c.size() > 0) {
				if (OcmIdUtil.INSTANCE.isEntityObject(c.iterator().next())) {
					return true;
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
			for (Object object2 : coll) {
				Member idMember = OcmIdUtil.INSTANCE.findIdMember(object2.getClass());
				Object id = OcmIdUtil.INSTANCE.getId(idMember, object2);
				if (id == null) {
					throw new IllegalStateException("Id must be set before being stored in a process: " + object2.getClass().getName() + "."
							+ idMember.getName());
				}
				os.writeUTF((String) id);
			}
		}
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
			Collection<String> ids = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				ids.add(is.readUTF());
			}
			ObjectContentManagerFactory emf = (ObjectContentManagerFactory) env.get(ObjectContentManagerFactory.OBJECT_CONTENT_MANAGER_FACTORY);
			ObjectContentManager em = emf.getCurrentObjectContentManager();
			for (String uuid : ids) {
				coll.add(em.getObjectByUuid(uuid));
			}
		}
		return coll;
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
