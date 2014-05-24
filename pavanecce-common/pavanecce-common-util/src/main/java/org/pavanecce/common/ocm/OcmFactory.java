package org.pavanecce.common.ocm;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;
import org.apache.jackrabbit.ocm.mapper.Mapper;

public class OcmFactory {
	public static final String OBJECT_CONTENT_MANAGER_FACTORY = OcmFactory.class.getName();
	private Repository repository;
	private String username;
	private String password;
	private Mapper mapper;
	private EventListener eventListener;
	private static ThreadLocal<ObjectContentManager> currentObjectContentManager = new ThreadLocal<ObjectContentManager>();

	public OcmFactory(Repository repository, String username, String password, Mapper mapper, EventListener eventListener) {
		super();
		this.repository = repository;
		this.username = username;
		this.password = password;
		this.mapper = mapper;
		this.eventListener = eventListener;
	}

	public ObjectContentManager createObjectContentManager() {
		try {
			Session session = repository.login(new SimpleCredentials(username, password.toCharArray()));
			if (eventListener != null) {
				session.getWorkspace().getObservationManager().addEventListener(eventListener, getEventMask(), "/", true, null, null, false);
			}
			ObjectContentManagerImpl result = new ObjectContentManagerImpl(session, mapper);
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

	protected int getEventMask() {
		return Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED | Event.PERSIST;
	}

	public ObjectContentManager getCurrentObjectContentManager() {
		ObjectContentManager objectContentManager = currentObjectContentManager.get();
		if (objectContentManager == null) {
			currentObjectContentManager.set(objectContentManager = createObjectContentManager());
		}
		return objectContentManager;
	}

	public void close(ObjectContentManager session) {
		if (session != null && session == getCurrentObjectContentManager() && session.getSession().isLive()) {
			session.logout();
		}
		currentObjectContentManager.set(null);

	}

	public EventListener getEventListener() {
		return this.eventListener;
	}


}
