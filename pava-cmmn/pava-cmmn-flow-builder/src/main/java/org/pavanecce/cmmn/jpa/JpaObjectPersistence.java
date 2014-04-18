package org.pavanecce.cmmn.jpa;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.pavanecce.cmmn.instance.ObjectPersistence;
import org.pavanecce.cmmn.test.domain.HousePlan;

public class JpaObjectPersistence implements ObjectPersistence {
	private UserTransaction transaction;
	EntityManager em;
	EntityManagerFactory emf;

	public JpaObjectPersistence(EntityManagerFactory emf2) {
		this.emf = emf2;
	}
	@Override
	public void start() {
		try {
			if (em != null && em.isOpen()) {
				em.close();
				em=null;
			}
			getTransaction().begin();
			getEntityManager().joinTransaction();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	@Override
	public void commit() {
		try {
			if (!isTransactionActive()) {
				getTransaction().begin();
				getEntityManager().joinTransaction();
			}
			getEntityManager().flush();
			getTransaction().commit();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	protected boolean isTransactionActive() throws SystemException, NamingException {
		int status = getTransaction().getStatus();
		return status==Status.STATUS_ACTIVE;
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
		getEntityManager().persist(o);
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

	@Override
	public <T> T find(Class<T> class1, Object id) {
		return getEntityManager().find(class1, id);
	}
	protected EntityManager getEntityManager(){
		if(em==null || !em.isOpen()){
			em=emf.createEntityManager();
			em.joinTransaction();
		}
		return em;
	}
	@Override
	public void remove(Object s) {
		getEntityManager().remove(s);
	}

}
