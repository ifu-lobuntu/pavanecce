package org.pavanecce.common.util;

public interface ObjectPersistence {

	void start();

	void commit();

	void persist(Object o);

	<T> T find(Class<T> class1, Object id);

	void remove(Object s);

	void close();

	void update(Object o);

	Object getDelegate();

}