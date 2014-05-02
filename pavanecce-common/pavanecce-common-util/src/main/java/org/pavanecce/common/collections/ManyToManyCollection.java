package org.pavanecce.common.collections;

import java.util.Collection;
import java.util.Iterator;

public abstract class ManyToManyCollection<P, C> extends TwoWayCollection<C> implements Collection<C> {
	private P parent;
	public ManyToManyCollection(P parent2) {
		this.parent=parent2;
	}

	protected abstract ManyToManyCollection<C, P> getOtherEnd(C child);

	@Override
	public boolean remove(Object o) {
		if (isInstanceOfChild(o)) {
			@SuppressWarnings("unchecked")
			C child = (C) o;
			ManyToManyCollection<C, P> otherEnd = getOtherEnd(child);
			if (otherEnd.removeImpl(parent)) {
				return removeImpl(child);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean add(C child) {
		ManyToManyCollection<C, P> otherEnd = getOtherEnd(child);
		if (otherEnd.contains(parent)) {
			return false;
		} else {
			otherEnd.addImpl(parent);
			return addImpl(child);
		}
	}

	@Override
	public Iterator<C> iterator() {
		final Iterator<C> delegate = getCurrent().iterator();
		return new Iterator<C>() {
			private C currentChild;

			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			@Override
			public C next() {
				return currentChild = delegate.next();
			}

			@Override
			public void remove() {
				delegate.remove();
				ManyToManyCollection<C, P> otherEnd = getOtherEnd(currentChild);
				otherEnd.removeImpl(parent);
				currentChild = null;
			}
		};
	}
}
