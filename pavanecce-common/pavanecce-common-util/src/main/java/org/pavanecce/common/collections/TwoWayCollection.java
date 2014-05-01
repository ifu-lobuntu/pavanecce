package org.pavanecce.common.collections;

import java.util.Collection;
import java.util.Iterator;

public abstract class TwoWayCollection<C> implements Collection<C>{

	protected Collection<C> current;

	public TwoWayCollection() {
		super();
	}

	protected abstract boolean isInstanceOfChild(Object o);

	protected abstract Collection<C> newInstance();

	protected abstract boolean addImpl(C e);

	protected abstract boolean removeImpl(C e);

	public <T> T[] toArray(T[] a) {
		return getCurrent().toArray(a);
	}

	public boolean containsAll(Collection<?> c) {
		boolean containsAll=true;
		for (Object object : c) {
			containsAll&=contains(object);
		}
		return containsAll;
	}

	@Override
	public boolean addAll(Collection<? extends C> c) {
		boolean changed = false;
		for (C e : c) {
			changed &= add(e);
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object e : c) {
			changed &= remove(e);
		}
		return changed;
	}

	@Override
	public int size() {
		return getCurrent().size();
	}

	protected Collection<C> getCurrent() {
		if (current == null) {
			current = newInstance();
		}
		return current;
	}

	@Override
	public boolean isEmpty() {
		return getCurrent().isEmpty();
	}

	@Override
	public void clear() {
		Iterator<C> iterator = iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	@Override
	public boolean contains(Object o) {
		return getCurrent().contains(o);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed=false;
		Iterator<C> iterator = iterator();
		while (iterator.hasNext()) {
			C c2 = (C) iterator.next();
			if(!c.contains(c2)){
				changed=true;
				iterator.remove();
			}
		}
		return changed;
	}

	@Override
	public Object[] toArray() {
		return getCurrent().toArray();
	}

}