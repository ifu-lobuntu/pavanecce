package org.pavanecce.common.collections;

import java.util.Collection;
import java.util.Iterator;

public abstract class OneToManyCollection<P, C> extends TwoWayCollection<C> implements Collection<C> {
	private P parent;

	public OneToManyCollection(P parent) {
		super();
		this.parent = parent;
	}

	protected abstract Collection<C> getDelegate() ;

	protected abstract P getParent(C child);

	protected abstract void setParent(C child, P parent);

	protected abstract Collection<C> getChildren(P parent);

	@Override
	public boolean remove(Object o) {
		if (isInstanceOfChild(o)) {
			@SuppressWarnings("unchecked")
			C child = (C) o;
			P oldParent = getParent(child);
			if (parent.equals(oldParent)) {
				setParent(child, null);
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
		P oldParent = getParent(child);
		if (!parent.equals(oldParent)) {
			if (oldParent != null) {
				getChildren(oldParent).remove(child);
			}
			setParent(child, parent);
			return addImpl(child);
		} else {
			return false;
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
				return currentChild=delegate.next();
			}

			@Override
			public void remove() {
				delegate.remove();
				setParent(currentChild, null);
				currentChild=null;
			}
		};
	}
}
