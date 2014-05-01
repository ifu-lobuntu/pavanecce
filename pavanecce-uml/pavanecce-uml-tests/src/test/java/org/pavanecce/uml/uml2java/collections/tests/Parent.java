package org.pavanecce.uml.uml2java.collections.tests;

import java.util.Collection;

import org.pavanecce.common.collections.OneToManySet;

public class Parent {
	OneToManySet<Parent, Child> children = new OneToManySet<Parent, Child>(this) {

		private static final long serialVersionUID = 4638662015627783124L;

		@Override
		protected boolean isLoaded() {
			return false;
		}

		@Override
		protected boolean isInstanceOfChild(Object o) {
			return o instanceof Child;
		}

		@Override
		protected Parent getParent(Child child) {
			return child.parent;
		}

		@Override
		protected void setParent(Child child, Parent parent) {
			child.parent = parent;
		}

		@Override
		protected Collection<Child> getChildren(Parent parent) {
			return parent.children;
		}
	};
}
