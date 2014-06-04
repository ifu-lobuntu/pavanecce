package org.pavanecce.cmmn.jbpm.jcr;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;

public class JcrObjectPersistenceFactory {
	public static final String OBJECT_CONTENT_MANAGER_FACTORY = JcrObjectPersistenceFactory.class.getName();
	private Repository repository;
	private String username;
	private String password;
	private EventListener eventListener;
	private static ThreadLocal<Session> currentObjectContentManager = new ThreadLocal<Session>();

	public JcrObjectPersistenceFactory(Repository repository, String username, String password, EventListener eventListener) {
		super();
		this.repository = repository;
		this.username = username;
		this.password = password;
		this.eventListener = eventListener;
	}

	public Session createSession() {
		try {
			Session session = repository.login(new SimpleCredentials(username, password.toCharArray()));
			if (eventListener != null) {
				session.getWorkspace().getObservationManager().addEventListener(eventListener, getEventMask(), "/", true, null, null, false);
			}
			currentObjectContentManager.set(session);
			return session;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected int getEventMask() {
		return Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED | Event.PERSIST;
	}

	public Session getCurrentObjectContentManager() {
		Session objectContentManager = currentObjectContentManager.get();
		if (objectContentManager == null) {
			currentObjectContentManager.set(objectContentManager = createSession());
		}
		return objectContentManager;
	}

	public void close(Session session) {
		if (session != null && session == getCurrentObjectContentManager() && session.isLive()) {
			session.logout();
		}
		currentObjectContentManager.set(null);

	}

	public EventListener getEventListener() {
		return this.eventListener;
	}

}
