package org.pavanecce.common.jpa;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.pavanecce.common.ObjectPersistence;

/**
 * This is a simple wrapper around JPA and JTA to allow demarcation of entityManagers and Transactions. It stores the
 * entityManager in a static ThreadLocal variable. This is needed to ensure that all object reads from different cases
 * can be read from the same entityManager. The command scoped entityManager is perfect for jBPM entities, but for
 * user-provided entities the narrow scope becomes a problem<BR>
 * NOT READY for a CMT environment (yet),
 * 
 */
public class JpaObjectPersistence implements ObjectPersistence {
	public static final String ENV_NAME = JpaObjectPersistence.class.getName() + "VAR";
	private UserTransaction transaction;
	static ThreadLocal<EntityManager> em = new ThreadLocal<EntityManager>();
	EntityManagerFactory emf;
	protected boolean startedTransaction = false;

	public JpaObjectPersistence(EntityManagerFactory emf2) {
		this.emf = emf2;
	}

	@Override
	public Object getDelegate() {
		return getEntityManager().getDelegate();
	}

	@Override
	public void start() {
		try {
			if (em.get() != null && em.get().isOpen()) {
				em.get().close();
				em.set(null);
			}
			startOrJoinTransaction();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	@Override
	public void commit() {
		try {
			startOrJoinTransaction();
			getEntityManager().flush();
			if (startedTransaction) {
				getTransaction().commit();
				this.startedTransaction = false;
			}
			close();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	protected void startOrJoinTransaction() {
		try {
			if (!isTransactionActive()) {
				this.startedTransaction = true;
				getTransaction().begin();
			}
			getEntityManager().joinTransaction();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	protected boolean isTransactionActive() throws SystemException, NamingException {
		int status = getTransaction().getStatus();
		return status == Status.STATUS_ACTIVE;
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
		startOrJoinTransaction();
		getEntityManager().persist(o);
	}

	public UserTransaction getTransaction() throws NamingException {
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

	public EntityManager getEntityManager() {
		if (em.get() == null || !em.get().isOpen()) {
			em.set(emf.createEntityManager());
			em.get().joinTransaction();
			em.get().setFlushMode(FlushModeType.COMMIT);
		}
		return em.get();
	}

	@Override
	public void remove(Object s) {
		getEntityManager().remove(s);
	}

	@Override
	public void close() {
		if (em.get() != null && em.get().isOpen()) {
			em.get().close();
		}
	}

	@Override
	public void update(Object o) {
	}

}
