package org.pavanecce.cmmn.ocm;

import java.util.Arrays;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.pavanecce.cmmn.instance.ObjectPersistence;

public class OcmObjectPersistence implements ObjectPersistence {
	private UserTransaction transaction;
	ObjectContentManager session;
	Repository repository;
	private AnnotationMapperImpl mapperImpl;


	public OcmObjectPersistence(Repository repository, Class<?> ... classes2) {
		this.repository = repository;
		List<Class> classes = Arrays.<Class>asList(classes2);
		mapperImpl = new AnnotationMapperImpl(classes);
	}


	public OcmObjectPersistence(ObjectContentManager p) {
		this.session=p;
	}


	@Override
	public void start() {
		try {
			if (session != null) {
				session.logout();
			}
			session = new ObjectContentManagerImpl(repository.login(new SimpleCredentials("admin", "admin".toCharArray())),mapperImpl);
//			getTransaction().begin();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	@Override
	public void commit() {
		try {
			if (!isTransactionActive()) {
				getTransaction().begin();
			}
			session.save();
//			getTransaction().commit();
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
		session.insert(o);
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
		return (T) session.getObjectByUuid((String) id);
	}


	@Override
	public void remove(Object s) {
		session.remove(s);
	}


	public Object find(String identifier) {
		return session.getObjectByUuid((String) identifier);
	}
	public Node findNode(String identifier) {
		try {
			return session.getSession().getNodeByIdentifier(identifier);
		} catch (ItemNotFoundException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return null;
	}

}
