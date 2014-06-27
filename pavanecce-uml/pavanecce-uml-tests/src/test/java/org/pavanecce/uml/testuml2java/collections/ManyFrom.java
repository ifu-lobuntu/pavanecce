package org.pavanecce.uml.testuml2java.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pavanecce.common.collections.ManyToManyCollection;
import org.pavanecce.common.collections.ManyToManySet;

public class ManyFrom {

	protected ManyToManySet<ManyFrom, ManyTo> many = new ManyToManySet<ManyFrom, ManyTo>(this) {

		private static final long serialVersionUID = 4411297325486752356L;

		@Override
		protected boolean isLoaded() {
			return false;
		}

		@Override
		protected ManyToManyCollection<ManyTo, ManyFrom> getOtherEnd(ManyTo child) {
			return child.many;
		}

		@Override
		protected boolean isInstanceOfChild(Object o) {
			return o instanceof ManyTo;
		}

		@Override
		protected Collection<ManyTo> getDelegate() {
			return delegate;
		}
	};
	Set<ManyTo> delegate = new HashSet<ManyTo>();

}
