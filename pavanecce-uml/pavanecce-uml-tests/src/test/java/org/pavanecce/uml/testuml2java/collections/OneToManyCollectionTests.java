package org.pavanecce.uml.testuml2java.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

public class OneToManyCollectionTests {
	@Test
	public void testAdd() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		Parent parent2 = new Parent();
		Child child2 = new Child();
		parent1.children.add(child1);
		assertSame(parent1, child1.parent);
		parent1.children.add(child2);
		assertEquals(2, parent1.children.size());
		assertSame(parent1, child2.parent);
		parent2.children.add(child2);
		assertEquals(1, parent1.children.size());
		assertEquals(1, parent2.children.size());
		assertTrue(parent1.children.contains(child1));
		assertFalse(parent1.children.contains(child2));
		assertSame(parent2, child2.parent);
	}

	@Test
	public void testAddAll() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		Parent parent2 = new Parent();
		Child child2 = new Child();
		Child child3 = new Child();
		parent1.children.add(child1);
		parent1.children.add(child2);
		parent2.children.add(child3);
		Collection<Child> toAdd = new HashSet<Child>(Arrays.asList(child2, child3));
		assertEquals(2, parent1.children.size());
		assertEquals(1, parent2.children.size());
		assertSame(parent2, child3.parent);
		parent1.children.addAll(toAdd);
		assertEquals(3, parent1.children.size());
		assertEquals(0, parent2.children.size());
		assertSame(parent1, child3.parent);
	}

	@Test
	public void testContainsAll() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		Parent parent2 = new Parent();
		Child child2 = new Child();
		Child child3 = new Child();
		parent1.children.add(child1);
		parent1.children.add(child2);
		parent2.children.add(child3);
		Collection<Child> falseTest = new HashSet<Child>(Arrays.asList(child2, child3));
		assertFalse(parent1.children.containsAll(falseTest));
		assertFalse(parent2.children.containsAll(falseTest));
		Collection<Child> trueTest1 = new HashSet<Child>(Arrays.asList(child1, child2));
		assertTrue(parent1.children.containsAll(trueTest1));
		Collection<Child> trueTest2 = new HashSet<Child>(Arrays.asList(child3));
		assertTrue(parent2.children.containsAll(trueTest2));
	}

	@Test
	public void testRemoveAll() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		Parent parent2 = new Parent();
		Child child2 = new Child();
		Child child3 = new Child();
		parent1.children.add(child1);
		parent1.children.add(child2);
		parent2.children.add(child3);
		Collection<Child> toRemove = new HashSet<Child>(Arrays.asList(child2, child3));
		assertEquals(2, parent1.children.size());
		assertEquals(1, parent2.children.size());
		assertSame(parent2, child3.parent);
		parent1.children.removeAll(toRemove);
		assertSame(parent2, child3.parent);
		assertEquals(1, parent1.children.size());
		assertEquals(1, parent2.children.size());
		parent2.children.removeAll(toRemove);
		assertNull(child2.parent);
		assertEquals(0, parent2.children.size());
		assertNull(child3.parent);
	}

	@Test
	public void testRemove() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		Child child2 = new Child();
		parent1.children.add(child1);
		parent1.children.add(child2);
		assertSame(parent1, child2.parent);
		assertEquals(2, parent1.children.size());
		parent1.children.remove(child2);
		assertNull(child2.parent);
		assertEquals(1, parent1.children.size());
		parent1.children.remove(parent1);
		assertEquals(1, parent1.children.size());
		assertTrue(parent1.children.contains(child1));
		assertFalse(parent1.children.contains(child2));
	}

	@Test
	public void testClear() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		parent1.children.add(child1);
		parent1.children.clear();
		assertNull(child1.parent);
		assertEquals(0, parent1.children.size());
	}

	@Test
	public void testRetainAll() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		Child child2 = new Child();
		parent1.children.add(child1);
		parent1.children.add(child2);
		parent1.children.retainAll(Arrays.asList(child1));
		assertNull(child2.parent);
		assertEquals(1, parent1.children.size());
	}

	@Test
	public void testRest() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		parent1.children.add(child1);
		assertFalse(parent1.children.isEmpty());
		assertSame(child1, parent1.children.toArray()[0]);
		assertSame(child1, parent1.children.toArray(new Child[1])[0]);
		Iterator<Child> iterator = parent1.children.iterator();
		assertTrue(iterator.hasNext());
		assertSame(child1, iterator.next());
		iterator.remove();
		assertNull(child1.parent);
		assertEquals(0, parent1.children.size());
	}

	@Test
	public void testLazyConsolidation() {
		Parent parent1 = new Parent();
		Child child1 = new Child();
		Child child2 = new Child();
		assertFalse(parent1.children.remove(child2));// because child2.parent!=parent1
		child2.parent = parent1;
		assertTrue(parent1.children.remove(child2));// because child2.parent==parent1
		assertFalse(parent1.children.contains(child2));
		assertFalse(parent1.children.isConsolidated());
		parent1.children.add(child1);
		parent1.children.add(child2);
		assertFalse(parent1.children.isConsolidated());
		assertTrue(parent1.children.contains(child2));
		assertFalse(parent1.children.isConsolidated());
		parent1.children.remove(child2);
		assertFalse(parent1.children.isConsolidated());
		assertFalse(parent1.children.contains(child2));
		assertFalse(parent1.children.isConsolidated());
		parent1.children.size();
		assertTrue(parent1.children.isConsolidated());
	}
}
