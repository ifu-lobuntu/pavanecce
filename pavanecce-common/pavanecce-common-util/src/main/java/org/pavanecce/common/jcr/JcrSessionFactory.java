package org.pavanecce.common.jcr;

import java.util.Collection;
import java.util.HashSet;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

public class JcrSessionFactory {
	public static final String JCR_SESSION_FACTORY = JcrSessionFactory.class.getName();
	private Repository repository;
	private String username;
	private String password;
	private EventListener eventListener;
	protected Session transientSession;
	private static ThreadLocal<Session> currentSession = new ThreadLocal<Session>();

	public JcrSessionFactory(Repository repository, String username, String password, EventListener eventListener) {
		this(username, password, eventListener);
		this.repository = repository;
	}

	public JcrSessionFactory(Session transientSession, EventListener eventListener) {
		this.transientSession = transientSession;
		this.eventListener = eventListener;
	}

	public JcrSessionFactory(String username, String password, EventListener eventListener) {
		super();
		this.username = username;
		this.password = password;
		this.eventListener = eventListener;
	}

	public final Session createSession() {
		try {
			Session session = transientSession;
			if (session == null) {
				session = repository.login(new SimpleCredentials(username, password.toCharArray()));
			}
			currentSession.set(session);
			updateEventListener();
			return session;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void updateEventListener() {
		try {
			if (eventListener != null && currentSession.get() != null) {
				boolean found = isListenerRegistered();
				if (!found) {
					removeOtherListenersAndAddCurrent();
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void removeOtherListenersAndAddCurrent() throws RepositoryException, UnsupportedRepositoryOperationException {
		ObservationManager om = currentSession.get().getWorkspace().getObservationManager();
		EventListenerIterator rels2 = om.getRegisteredEventListeners();
		Collection<EventListener> existing = new HashSet<EventListener>();
		while (rels2.hasNext()) {
			EventListener nel = rels2.nextEventListener();
			String className = nel.getClass().getName();
			if (className.startsWith("org.pavanecce.cmmn.jbpm.jcr") || className.startsWith("org.pavanecce.cmmn.jbpm.ocm")) {
				existing.add(nel);
			}
		}
		for (EventListener el : existing) {
			om.removeEventListener(el);
		}
		currentSession.get().getWorkspace().getObservationManager().addEventListener(eventListener, getEventMask(), "/", true, null, null, false);
	}

	protected boolean isListenerRegistered() throws RepositoryException, UnsupportedRepositoryOperationException {
		EventListenerIterator rels = currentSession.get().getWorkspace().getObservationManager().getRegisteredEventListeners();
		boolean found = false;
		while (rels.hasNext()) {
			EventListener nextEventListener = rels.nextEventListener();
			if (eventListener == nextEventListener) {
				found = true;
				break;
			}
		}
		return found;
	}

	protected final int getEventMask() {
		int nodeEvents = Event.NODE_ADDED | Event.NODE_REMOVED;
		int propertyEvents = Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED;
		return nodeEvents | propertyEvents | Event.PERSIST;
	}

	public final Session getCurrentSession() {
		Session objectContentManager = currentSession.get();
		if (objectContentManager == null) {
			currentSession.set(objectContentManager = createSession());
		}
		return objectContentManager;
	}

	public final void close(Session session) {
		if (session != null && session == getCurrentSession() && session.isLive() && session != transientSession) {
			session.logout();
		}
		currentSession.set(null);
	}

	public final EventListener getEventListener() {
		return this.eventListener;
	}

}
