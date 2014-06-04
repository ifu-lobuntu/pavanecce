package org.pavanecce.common.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ManyToManySet<P, C> extends ManyToManyCollection<P, C> implements Set<C>, Serializable {

	private static final long serialVersionUID = 6699029439985008351L;
	private List<C> additions;
	private List<C> removals;
	private boolean isConsolidated = false;

	public ManyToManySet(P parent) {
		super(parent);
	}

	protected abstract boolean isLoaded();

	protected abstract Collection<C> getDelegate();

	@Override
	public boolean removeImpl(C child) {
		if (useTempCollections()) {
			if (additions != null && additions.contains(child)) {
				additions.remove(child);
			}
			return getRemovals().add(child);
		} else {
			return getCurrent().remove(child);
		}
	}

	@Override
	public boolean addImpl(C child) {
		if (useTempCollections()) {
			if (removals != null && removals.contains(child)) {
				removals.remove(child);
			}
			return getAdditions().add(child);
		} else {
			return getCurrent().add(child);
		}
	}

	@Override
	protected Collection<C> newInstance() {
		return new HashSet<C>();
	}

	private boolean useTempCollections() {
		return !(isLoaded() || isConsolidated());
	}

	public final boolean isConsolidated() {
		return isConsolidated;
	}

	@Override
	public Set<C> getCurrent() {
		isConsolidated = true;
		Collection<C> current = getDelegate();
		if (additions != null) {
			current.addAll(additions);
			additions = null;
		}
		if (removals != null) {
			current.removeAll(removals);
			removals = null;
		}
		return (Set<C>) current;
	}

	@Override
	public boolean contains(Object child) {
		if (useTempCollections()) {
			if (additions != null && additions.contains(child)) {
				return true;
			} else if (removals == null || removals.contains(child)) {
				return false;
			}
		}
		return getCurrent().contains(child);
	}

	private List<C> getAdditions() {
		if (this.additions == null) {
			this.additions = new ArrayList<C>();
		}
		return this.additions;
	}

	private List<C> getRemovals() {
		if (this.removals == null) {
			this.removals = new ArrayList<C>();
		}
		return this.removals;
	}

}
