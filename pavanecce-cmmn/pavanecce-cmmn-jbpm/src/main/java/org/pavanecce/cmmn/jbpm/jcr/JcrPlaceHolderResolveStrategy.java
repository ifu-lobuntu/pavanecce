package org.pavanecce.cmmn.jbpm.jcr;

import static org.pavanecce.cmmn.jbpm.jcr.JcrUtil.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jcr.Node;

import org.drools.core.common.DroolsObjectInputStream;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.kie.api.runtime.Environment;

public class JcrPlaceHolderResolveStrategy extends JPAPlaceholderResolverStrategy {
	private Environment env;

	public JcrPlaceHolderResolveStrategy(Environment env) {
		super(env);
		this.env = env;
	}

	@Override
	public boolean accept(Object object) {
		return object instanceof Node;
	}

	@Override
	public void write(ObjectOutputStream os, Object node) throws IOException {
		try {
			os.writeUTF(((Node) node).getIdentifier());
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	@Override
	public Object read(ObjectInputStream is) throws IOException, ClassNotFoundException {
		try {
			String uuid = is.readUTF();
			JcrObjectPersistenceFactory f = (JcrObjectPersistenceFactory) env.get(JcrObjectPersistenceFactory.OBJECT_CONTENT_MANAGER_FACTORY);
			return f.getCurrentObjectContentManager().getNodeByIdentifier(uuid);
		} catch (Exception e) {
			throw convertException(e);
		}
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
