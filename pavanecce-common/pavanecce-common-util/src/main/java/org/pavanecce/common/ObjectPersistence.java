package org.pavanecce.common;


public interface ObjectPersistence {

	public abstract void start();

	public abstract void commit();

	public abstract void persist(Object o);

	public abstract <T> T find(Class<T> class1, Object id);

	public abstract void remove(Object s);

	public abstract void close();

	void update(Object o);

}