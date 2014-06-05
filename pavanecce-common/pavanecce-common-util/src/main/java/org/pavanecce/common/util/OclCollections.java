package org.pavanecce.common.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class OclCollections {
	public static final Timestamp FUTURE = new Timestamp(1000L * 60 * 60 * 24 * 365 * 1000);

	public static <T> Set<T> makeCopy(Set<T> source) {
		return new HashSet<T>(source);
	}

	public static <T> List<T> sortBy(Collection<T> source, Comparator<T> c) {
		ArrayList<T> result = new ArrayList<T>(source);
		Collections.sort(result, c);
		return result;
	}

	public static void rangeOf(Collection<Integer> c, Integer first, Integer last) {
		for (int i = first; i <= last; i++) {
			c.add(i);
		}
	}

	public static <T> List<T> makeCopy(List<T> source) {
		return new ArrayList<T>(source);
	}

	public static <T> Collection<T> makeCopy(Collection<T> source) {
		return new ArrayList<T>(source);
	}

	public static <T> boolean bagEquals(Collection<T> source, Collection<T> arg) {
		if (source.size() != arg.size()) {
			return false;
		}
		Collection<T> myArg = new ArrayList<T>(arg);
		for (T elem : source) {
			if (myArg.contains(elem)) {
				myArg.remove(elem);
			} else {
				return false;
			}
		}
		return myArg.isEmpty();
	}

	public static Collection bagFlatten(Object source) {
		Collection result = new ArrayList();
		if (source instanceof Collection) {
			Iterator it = ((Collection) source).iterator();
			while (it.hasNext()) {
				Object elem = it.next();
				if (elem instanceof Collection) {
					result.addAll(bagFlatten(elem));
				} else {
					result.add(elem);
				}
			}
		}
		return result;
	}

	public static <T> Collection<T> collectionAsBag(Collection<T> myCollection) {
		Collection<T> result = new ArrayList<T>();
		if (myCollection != null) {
			result.addAll(myCollection);
		}
		return result;
	}

	public static <T> List<T> collectionAsOrderedSet(Collection<T> myCollection) {
		List<T> result = new ArrayList<T>();
		if (myCollection != null) {
			result.addAll(myCollection);
		}
		return result;
	}

	public static <T> List<T> collectionAsSequence(Collection<T> myCollection) {
		List<T> result = new ArrayList<T>();
		if (myCollection != null) {
			result.addAll(myCollection);
		}
		return result;
	}

	public static <T> Set<T> collectionAsSet(Collection<T> myCollection) {
		Set<T> result = new HashSet<T>();
		if (myCollection != null) {
			result.addAll(myCollection);
		}
		return result;
	}

	public static <T> T collectionAsSingleObject(Collection<T> in) {
		if (in.size() == 0) {
			return null;
		} else {
			if (in.size() == 1) {
				return in.iterator().next();
			} else {
				throw new IllegalArgumentException("Input collection contains more than one object: " + in.size());
			}
		}
	}

	public static boolean includesAll(Collection source, Collection arg) {
		return source.containsAll(arg);
	}

	public static boolean includes(Collection source, Object arg) {
		return source.contains(arg);
	}

	public static boolean excludes(Collection source, Object arg) {
		return !source.contains(arg);
	}

	public static boolean excludesAll(Collection source, Collection arg) {
		Iterator it = arg.iterator();
		while (it.hasNext()) {
			Object elem = it.next();
			if (source.contains(elem)) {
				return false;
			}
		}
		return true;
	}

	public static <T> Collection<T> excluding(Collection<? extends T> mySource, T myElem) {
		Collection<T> result = new ArrayList<T>(mySource);
		if (myElem != null) {
			result.remove(myElem);
		}
		return result;
	}

	public static <T> List<T> excluding(List<? extends T> mySource, T myElem) {
		List<T> result = new ArrayList<T>(mySource);
		if (myElem != null) {
			result.remove(myElem);
		}
		return result;
	}

	public static <T> Set<T> excluding(Set<T> mySource, T myElem) {
		Set<T> result = new HashSet<T>(mySource);
		if (myElem != null) {
			result.remove(myElem);
		}
		return result;
	}

	public static <T> Collection<T> including(Collection<T> mySource, T myElem) {
		Collection<T> result = new ArrayList<T>(mySource);
		if (myElem != null) {
			result.add(myElem);
		}
		return result;
	}

	public static <T> List<T> including(List<T> mySource, T myElem) {
		List<T> result = new ArrayList<T>(mySource);
		if (myElem != null) {
			result.add(myElem);
		}
		return result;
	}

	public static <T> Set<T> including(Set<T> mySource, T myElem) {
		Set<T> result = new HashSet<T>(mySource);
		if (myElem != null) {
			result.add(myElem);
		}
		return result;
	}

	public static <T> List<T> insertAt(List<T> mySource, int myIndex, T myElem) {
		if (mySource == null) {
			return null;
		}
		if (myElem != null) {
			mySource.add(myIndex, myElem);
		}
		return mySource;
	}

	public static Collection<Integer> intAsBag(Integer myInt) {
		Collection<Integer> result = new ArrayList<Integer>();
		if (myInt != null) {
			result.add(myInt);
		}
		return result;
	}

	public static List<Integer> intAsOrderedSet(Integer myInt) {
		List<Integer> result = new ArrayList<Integer>();
		if (myInt != null) {
			result.add(myInt);
		}
		return result;
	}

	public static List<Integer> intAsSequence(Integer myInt) {
		List<Integer> result = new ArrayList<Integer>();
		if (myInt != null) {
			result.add(myInt);
		}
		return result;
	}

	public static Set<Integer> intAsSet(Integer myInt) {
		Set<Integer> result = new HashSet<Integer>();
		if (myInt != null) {
			result.add(myInt);
		}
		return result;
	}

	public static <T> Set<T> intersection(Collection<T> mySource, Collection<T> myElem) {
		Set<T> result = new TreeSet<T>(mySource);
		if (myElem != null) {
			result.retainAll(myElem);
		}
		return result;
	}

	public static <T> Collection<T> objectAsBag(T myObject) {
		Collection<T> result = new ArrayList<T>();
		if (myObject != null) {
			result.add(myObject);
		}
		return result;
	}

	public static <T> List<T> objectAsOrderedSet(T myObject) {
		List<T> result = new ArrayList<T>();
		if (myObject != null) {
			result.add(myObject);
		}
		return result;
	}

	public static <T> List<T> objectAsSequence(T myObject) {
		List<T> result = new ArrayList<T>();
		if (myObject != null) {
			result.add(myObject);
		}
		return result;
	}

	public static <T> Set<T> objectAsSet(T myObject) {
		Set<T> result = new HashSet<T>();
		if (myObject != null) {
			result.add(myObject);
		}
		return result;
	}

	public static boolean orderedsetEquals(List source, List arg) {
		if (source.size() != arg.size()) {
			return false;
		}
		Iterator it1 = source.iterator();
		Iterator it2 = arg.iterator();
		while (it1.hasNext()) {
			Object elem1 = it1.next();
			Object elem2 = it2.next();
			if (elem1 instanceof Integer) {
				if (((Integer) elem1).intValue() != ((Integer) elem2).intValue()) {
					return false;
				}
			} else {
				if (elem1 instanceof Float) {
					if (((Float) elem1).floatValue() != ((Float) elem2).floatValue()) {
						return false;
					}
				} else {
					if (elem1 instanceof Boolean) {
						if (((Boolean) elem1).booleanValue() != ((Boolean) elem2).booleanValue()) {
							return false;
						}
					} else {
						if (!elem1.equals(elem2)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public static List orderedsetFlatten(Object source) {
		List result = new ArrayList();
		if (source instanceof Collection) {
			Iterator it = ((Collection) source).iterator();
			while (it.hasNext()) {
				Object elem = it.next();
				if (elem instanceof Collection) {
					result.addAll(orderedsetFlatten(elem));
				} else {
					result.add(elem);
				}
			}
		}
		return result;
	}

	public static Collection<Double> realAsBag(Double myReal) {
		Collection<Double> result = new ArrayList<Double>();
		if (myReal != null) {
			result.add(myReal);
		}
		return result;
	}

	public static List<Double> realAsOrderedSet(Double myReal) {
		List<Double> result = new ArrayList<Double>();
		if (myReal != null) {
			result.add(myReal);
		}
		return result;
	}

	public static List<Double> realAsSequence(Double myReal) {
		List<Double> result = new ArrayList<Double>();
		if (myReal != null) {
			result.add(myReal);
		}
		return result;
	}

	public static Set<Double> realAsSet(Double myReal) {
		Set<Double> result = new HashSet<Double>();
		if (myReal != null) {
			result.add(myReal);
		}
		return result;
	}

	public static boolean sequenceEquals(List source, List arg) {
		if (source.size() != arg.size()) {
			return false;
		}
		Iterator it1 = source.iterator();
		Iterator it2 = arg.iterator();
		while (it1.hasNext()) {
			Object elem1 = it1.next();
			Object elem2 = it2.next();
			if (elem1 instanceof Integer) {
				if (((Integer) elem1).intValue() != ((Integer) elem2).intValue()) {
					return false;
				}
			} else {
				if (elem1 instanceof Float) {
					if (((Float) elem1).floatValue() != ((Float) elem2).floatValue()) {
						return false;
					}
				} else {
					if (elem1 instanceof Boolean) {
						if (((Boolean) elem1).booleanValue() != ((Boolean) elem2).booleanValue()) {
							return false;
						}
					} else {
						if (!elem1.equals(elem2)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public static List sequenceFlatten(Object source) {
		List result = new ArrayList();
		if (source instanceof Collection) {
			Iterator it = ((Collection) source).iterator();
			while (it.hasNext()) {
				Object elem = it.next();
				if (elem instanceof Collection) {
					result.addAll(sequenceFlatten(elem));
				} else {
					result.add(elem);
				}
			}
		}
		return result;
	}

	public static boolean setEquals(Set source, Set arg) {
		if (source.size() != arg.size()) {
			return false;
		}
		Iterator it = arg.iterator();
		while (it.hasNext()) {
			Object elem = it.next();
			if (!source.contains(elem)) {
				return false;
			}
		}
		return true;
	}

	public static Set setFlatten(Object source) {
		Set result = new HashSet();
		if (source instanceof Collection) {
			Iterator it = ((Collection) source).iterator();
			while (it.hasNext()) {
				Object elem = it.next();
				if (elem instanceof Collection) {
					result.addAll(setFlatten(elem));
				} else {
					result.add(elem);
				}
			}
		}
		return result;
	}

	public static Collection<String> stringAsBag(String myString) {
		Collection<String> result = new ArrayList<String>();
		if (myString != null) {
			result.add(myString);
		}
		return result;
	}

	public static List<String> stringAsOrderedSet(String myString) {
		List<String> result = new ArrayList<String>();
		if (myString != null) {
			result.add(myString);
		}
		return result;
	}

	public static List<String> stringAsSequence(String myString) {
		List<String> result = new ArrayList<String>();
		if (myString != null) {
			result.add(myString);
		}
		return result;
	}

	public static <T> List<T> emptyOrderedSet() {
		return Collections.<T> emptyList();
	}

	public static <T> List<T> emptySequence() {
		return Collections.<T> emptyList();
	}

	public static <T> Set<T> emptySet() {
		return Collections.<T> emptySet();
	}

	public static <T> Collection<T> emptyBag() {
		return Collections.<T> emptyList();
	}

	public static Set<String> stringAsSet(String myString) {
		Set<String> result = new HashSet<String>();
		if (myString != null) {
			result.add(myString);
		}
		return result;
	}

}