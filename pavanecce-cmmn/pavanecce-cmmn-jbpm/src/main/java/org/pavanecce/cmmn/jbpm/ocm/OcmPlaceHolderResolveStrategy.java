package org.pavanecce.cmmn.jbpm.ocm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Member;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.drools.core.common.DroolsObjectInputStream;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.kie.api.runtime.Environment;
import org.pavanecce.common.ocm.ObjectContentManagerFactory;

public class OcmPlaceHolderResolveStrategy extends JPAPlaceholderResolverStrategy {
	private Environment env;

	public OcmPlaceHolderResolveStrategy(Environment env) {
		super(env);
		this.env = env;
	}

	@Override
	public boolean accept(Object object) {
		return OcmIdUtil.INSTANCE.isEntityObject(object);
	}

	@Override
	public void write(ObjectOutputStream os, Object node) throws IOException {
		Member idMember = OcmIdUtil.INSTANCE.findIdMember(node.getClass());
		Object id = OcmIdUtil.INSTANCE.getId(idMember, node);
		if (id == null) {
			throw new IllegalStateException("Id must be set before being stored in a process: " + node.getClass().getName() + "." + idMember.getName());
		}
		os.writeUTF((String) id);
	}

	@Override
	public Object read(ObjectInputStream is) throws IOException, ClassNotFoundException {
		String uuid = is.readUTF();
		ObjectContentManagerFactory emf = (ObjectContentManagerFactory) env.get(ObjectContentManagerFactory.OBJECT_CONTENT_MANAGER_FACTORY);
		ObjectContentManager em = emf.getCurrentObjectContentManager();
		return em.getObjectByUuid(uuid);
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
