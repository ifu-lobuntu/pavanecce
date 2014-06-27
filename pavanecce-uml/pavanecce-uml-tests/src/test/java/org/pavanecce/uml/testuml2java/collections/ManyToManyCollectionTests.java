package org.pavanecce.uml.testuml2java.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

public class ManyToManyCollectionTests {
	@Test
	public void testAdd() {
		ManyFrom manyFrom1 = new ManyFrom();
		ManyTo manyTo1 = new ManyTo();
		ManyFrom manyFrom2 = new ManyFrom();
		ManyTo manyTo2 = new ManyTo();
		manyFrom1.many.add(manyTo1);
		assertTrue(manyTo1.many.contains(manyFrom1));
		assertTrue(manyFrom1.many.contains(manyTo1));
		assertEquals(1, manyFrom1.many.size());
		manyFrom1.many.add(manyTo2);
		assertTrue(manyTo2.many.contains(manyFrom1));
		assertTrue(manyFrom1.many.contains(manyTo2));
		assertEquals(2, manyFrom1.many.size());
		manyFrom2.many.add(manyTo2);
		assertEquals(2, manyTo2.many.size());
		assertEquals(2, manyTo2.delegate.size());
		assertEquals(2, manyFrom1.many.size());
		assertEquals(2, manyFrom1.delegate.size());
		assertEquals(1, manyFrom2.many.size());
		assertEquals(1, manyFrom2.delegate.size());
		assertTrue(manyTo2.many.contains(manyFrom2));
		assertTrue(manyFrom2.many.contains(manyTo2));
		assertTrue(manyFrom1.many.contains(manyTo1));
		assertTrue(manyFrom1.many.contains(manyTo2));
	}

	@Test
	public void testAddAll() {
		ManyFrom manyFrom1 = new ManyFrom();
		ManyFrom manyFrom2 = new ManyFrom();
		ManyTo manyTo1 = new ManyTo();
		ManyTo manyTo2 = new ManyTo();
		ManyTo manyTo3 = new ManyTo();
		manyFrom1.many.add(manyTo1);
		manyFrom1.many.add(manyTo2);
		manyFrom2.many.add(manyTo3);
		Collection<ManyTo> toAdd = new HashSet<ManyTo>(Arrays.asList(manyTo2, manyTo3));
		assertEquals(2, manyFrom1.many.size());
		assertEquals(2, manyFrom1.delegate.size());
		assertEquals(1, manyFrom2.many.size());
		assertEquals(1, manyFrom2.delegate.size());
		assertTrue(manyTo3.many.contains(manyFrom2));
		manyFrom1.many.addAll(toAdd);
		assertEquals(3, manyFrom1.many.size());
		assertEquals(3, manyFrom1.delegate.size());
		assertEquals(1, manyFrom2.many.size());
		assertEquals(1, manyFrom2.delegate.size());
		assertEquals(2, manyTo3.many.size());
		assertEquals(2, manyTo3.delegate.size());
		assertTrue(manyTo3.many.contains(manyFrom1));
	}

	@Test
	public void testRemove() {
		ManyFrom manyFrom1 = new ManyFrom();
		ManyTo manyTo1 = new ManyTo();
		ManyTo manyTo2 = new ManyTo();
		manyFrom1.many.add(manyTo1);
		manyFrom1.many.add(manyTo2);
		assertEquals(2, manyFrom1.many.size());
		manyFrom1.many.remove(manyTo2);
		assertFalse(manyTo2.many.contains(manyFrom1));
		assertFalse(manyFrom1.many.contains(manyTo2));
		assertEquals(1, manyFrom1.many.size());
		assertEquals(1, manyFrom1.delegate.size());
		assertEquals(1, manyTo1.many.size());
		assertEquals(1, manyTo1.delegate.size());
		assertEquals(0, manyTo2.many.size());
		assertEquals(0, manyTo2.delegate.size());
		assertFalse(manyFrom1.many.remove(manyFrom1));
		assertEquals(1, manyFrom1.many.size());
		assertEquals(1, manyFrom1.delegate.size());
	}

	@Test
	public void testRemoveAll() {
		ManyFrom manyFrom1 = new ManyFrom();
		ManyTo manyTo1 = new ManyTo();
		ManyFrom manyFrom2 = new ManyFrom();
		ManyTo manyTo2 = new ManyTo();
		ManyTo manyTo3 = new ManyTo();
		manyFrom1.many.add(manyTo1);
		manyFrom1.many.add(manyTo2);
		manyFrom2.many.add(manyTo3);
		Collection<ManyTo> toRemove = new HashSet<ManyTo>(Arrays.asList(manyTo2, manyTo3));
		assertEquals(2, manyFrom1.many.size());
		assertEquals(2, manyFrom1.delegate.size());
		assertEquals(1, manyFrom2.many.size());
		assertEquals(1, manyFrom2.delegate.size());
		assertTrue(manyTo3.many.contains(manyFrom2));
		manyFrom1.many.removeAll(toRemove);
		assertTrue(manyTo3.many.contains(manyFrom2));
		assertEquals(1, manyFrom1.many.size());
		assertEquals(1, manyFrom1.delegate.size());
		assertEquals(1, manyFrom2.many.size());
		assertEquals(1, manyFrom2.delegate.size());
		manyFrom2.many.removeAll(toRemove);
		assertTrue(manyTo2.many.isEmpty());
		assertTrue(manyTo2.delegate.isEmpty());
		assertTrue(manyTo3.many.isEmpty());
		assertTrue(manyTo3.delegate.isEmpty());
	}

	@Test
	public void testIterator() {
		ManyFrom manyFrom1 = new ManyFrom();
		ManyTo manyTo1 = new ManyTo();
		manyFrom1.many.add(manyTo1);
		Iterator<ManyFrom> iterator = manyTo1.many.iterator();
		while (iterator.hasNext()) {
			ManyFrom manyFrom = iterator.next();
			if (manyFrom == manyFrom1) {
				iterator.remove();
			}
		}
		assertTrue(manyFrom1.many.isEmpty());
		assertTrue(manyFrom1.delegate.isEmpty());
		assertTrue(manyTo1.many.isEmpty());
		assertTrue(manyTo1.delegate.isEmpty());
	}
}
