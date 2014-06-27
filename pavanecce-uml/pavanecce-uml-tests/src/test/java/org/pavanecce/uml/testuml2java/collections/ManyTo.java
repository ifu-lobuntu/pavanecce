package org.pavanecce.uml.testuml2java.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pavanecce.common.collections.ManyToManyCollection;
import org.pavanecce.common.collections.ManyToManySet;

public class ManyTo {
	ManyToManySet<ManyTo, ManyFrom> many = new ManyToManySet<ManyTo, ManyFrom>(this) {

		private static final long serialVersionUID = -6358861902092046628L;

		@Override
		protected boolean isLoaded() {
			return false;
		}

		@Override
		protected ManyToManyCollection<ManyFrom, ManyTo> getOtherEnd(ManyFrom child) {
			return child.many;
		}

		@Override
		protected boolean isInstanceOfChild(Object o) {
			return o instanceof ManyFrom;
		}

		@Override
		protected Collection<ManyFrom> getDelegate() {
			return delegate;
		}
	};
	Set<ManyFrom> delegate = new HashSet<ManyFrom>();
}
