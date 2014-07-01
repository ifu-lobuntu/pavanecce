package org.pavanecce.cmmn.jbpm.jcr;

import static org.pavanecce.cmmn.jbpm.jcr.JcrUtil.convertException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.jcr.Node;

import org.drools.core.common.DroolsObjectInputStream;
import org.kie.api.runtime.Environment;
import org.pavanecce.cmmn.jbpm.jpa.JpaCollectionPlaceHolderResolverStrategy;
import org.pavanecce.common.jcr.JcrSessionFactory;

public class JcrCollectionPlaceHolderResolveStrategy extends JpaCollectionPlaceHolderResolverStrategy {
	private Environment env;

	public JcrCollectionPlaceHolderResolveStrategy(Environment env) {
		super(env);
		this.env = env;
	}

	@Override
	public boolean accept(Object object) {
		if (object instanceof Collection) {
			Collection<?> c = (Collection<?>) object;
			if (c.size() > 0) {
				if (c.iterator().next() instanceof Node) {
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
				try {
					os.writeUTF(((Node) object2).getIdentifier());
				} catch (Exception e) {
					throw convertException(e);
				}
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
			JcrSessionFactory f = (JcrSessionFactory) env.get(JcrSessionFactory.JCR_SESSION_FACTORY);
			for (String uuid : ids) {
				try {
					coll.add(f.getCurrentSession().getNodeByIdentifier(uuid));
				} catch (Exception e) {
					throw convertException(e);
				}
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
