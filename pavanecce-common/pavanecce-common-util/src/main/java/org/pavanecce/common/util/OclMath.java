package org.pavanecce.common.util;

public class OclMath {
	public static Integer abs(Integer i) {
		if (i == null) {
			return null;
		} else {
			return Math.abs(i);
		}
	}
	public static Double abs(Double i) {
		if (i == null) {
			return null;
		} else {
			return Math.abs(i);
		}
	}
	public static Double floor(Double i) {
		if (i == null) {
			return null;
		} else {
			return Math.floor(i);
		}
	}
	public static Integer round(Double i) {
		if (i == null) {
			return null;
		} else {
			return new Integer((int) Math.round(i));
		}
	}
	public static Double max(Double i, Double j) {
		if (i == null) {
			return j;
		}else if(j==null){
			return i;
		} else {
			return Math.max(i,j);
		}
	}
	public static Integer max(Integer i, Integer j) {
		if (i == null) {
			return j;
		}else if(j==null){
			return i;
		} else {
			return Math.max(i,j);
		}
	}

}
