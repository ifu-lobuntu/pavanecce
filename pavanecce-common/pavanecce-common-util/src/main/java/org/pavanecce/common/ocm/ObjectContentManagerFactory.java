package org.pavanecce.common.ocm;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.observation.EventListener;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;
import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.pavanecce.common.jcr.JcrSessionFactory;

public class ObjectContentManagerFactory extends JcrSessionFactory {
	public static final String OBJECT_CONTENT_MANAGER_FACTORY = ObjectContentManagerFactory.class.getName();
	private static ThreadLocal<ObjectContentManager> currentObjectContentManager = new ThreadLocal<ObjectContentManager>();
	private Mapper mapper;

	public ObjectContentManagerFactory(Repository repository, String username, String password, Mapper mapper, EventListener eventListener) {
		super(repository, username, password, eventListener);
		this.mapper = mapper;
	}

	public ObjectContentManagerFactory(Session transientSession, Mapper mapper, EventListener eventListener) {
		super(transientSession, eventListener);
		this.mapper = mapper;
	}

	public ObjectContentManager createObjectContentManager() {
		try {
			ObjectContentManagerImpl result = new ObjectContentManagerImpl(super.getCurrentSession(), mapper);
			currentObjectContentManager.set(result);
			return result;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Mapper getMapper() {
		return mapper;
	}

	public ObjectContentManager getCurrentObjectContentManager() {
		ObjectContentManager objectContentManager = currentObjectContentManager.get();
		if (objectContentManager == null) {
			currentObjectContentManager.set(objectContentManager = createObjectContentManager());
		}
		return objectContentManager;
	}

	public void close(ObjectContentManager session) {
		if (session != null && session == getCurrentObjectContentManager() && session.getSession().isLive() && session.getSession() != transientSession) {
			session.logout();
		}
		currentObjectContentManager.set(null);
	}
}
