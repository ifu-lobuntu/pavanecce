package org.pavanecce.common;

public class Stopwatch {
	private static long start;
	private static long lastLap;

	public static void start() {
		lastLap = start = System.currentTimeMillis();
	}

	public static void lap(String name) {
//		System.out.println(name + " took " + (System.currentTimeMillis() - lastLap));
		lastLap = System.currentTimeMillis();
	}
}
