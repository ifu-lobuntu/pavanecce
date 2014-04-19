package org.pavanecce.cmmn.ocm;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.jackrabbit.ocm.exception.JcrMappingException;
import org.apache.jackrabbit.ocm.exception.ObjectContentManagerException;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.pavanecce.cmmn.instance.ObjectPersistence;

public class OcmObjectPersistence implements ObjectPersistence {
	private UserTransaction transaction;
	private OcmFactory factory;
	private boolean startedTransaction = false;

	public OcmObjectPersistence(OcmFactory factory) {
		this.factory = factory;
	}

	@Override
	public void start() {
		try {

			startTransaction();

		} catch (Exception e) {
			throw convertException(e);
		}
	}

	protected void startTransaction() throws SystemException, NamingException, NotSupportedException {
		if (!isTransactionActive()) {
			getTransaction().begin();
			this.startedTransaction = true;
		}
	}

	@Override
	public void commit() {
		try {
			startTransaction();
			getSession().save();
			if (startedTransaction) {
				getTransaction().commit();
			}
			startedTransaction = false;
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	protected boolean isTransactionActive() throws SystemException, NamingException {
		return getTransaction().getStatus() == Status.STATUS_ACTIVE;
	}

	protected RuntimeException convertException(Exception e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	@Override
	public void persist(Object o) {
		getSession().insert(o);
	}

	@Override
	public void update(Object o) {
		getSession().update(o);
	}

	public ObjectContentManager getSession() {
		return factory.getCurrentObjectContentManager();
	}

	UserTransaction getTransaction() throws NamingException {
		if (transaction == null) {
			transaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		}
		return transaction;
	}

	void setTransaction(UserTransaction transaction) {
		this.transaction = transaction;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> class1, Object id) {
		if (id instanceof OcmCaseSubscriptionKey) {
			id = ((OcmCaseSubscriptionKey) id).getId();
			return (T) getSession().getObject("/subscriptions/" + id);
		}
		return (T) getSession().getObjectByUuid((String) id);
	}

	@Override
	public void remove(Object s) {
		getSession().remove(s);
	}

	public Object find(String identifier) {
		return getSession().getObjectByUuid((String) identifier);
	}

	public Node findNode(String identifier) {
		try {
			return getSession().getSession().getNodeByIdentifier(identifier);
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() {
		factory.close(getSession());
	}

}
