package org.pavanecce.common.util;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stopwatch {
	private final Logger logger;
	private long start;
	private long lastLap;

	public Stopwatch() {
		this(Stopwatch.class);
	}

	public Stopwatch(Class<?> source) {
		logger = LoggerFactory.getLogger(source);
	}

	public void start() {
		lastLap = start = System.currentTimeMillis();
	}

	public long lap(String name) {
		long result = (System.currentTimeMillis() - lastLap);
		logger.debug("{} took {}", name, result);
		lastLap = System.currentTimeMillis();
		return result;
	}

	public void lap(String name, long threshHold, TimeUnit tu) {
		long dur = lap(name);
		if (logger.isDebugEnabled()) {
			long thresholdMillis = TimeUnit.MILLISECONDS.convert(threshHold, tu);
			if (dur > thresholdMillis) {
				throw new IllegalStateException("Lap " + name + " took longer than " + threshHold + " " + tu.name().toLowerCase());
			}
		}
	}

	public void finish(String name, long threshHold, TimeUnit tu) {
		long dur = finish(name);
		if (logger.isDebugEnabled()) {
			long thresholdMillis = TimeUnit.MILLISECONDS.convert(threshHold, tu);
			if (dur > thresholdMillis) {
				throw new IllegalStateException(name + " took longer than " + threshHold + " " + tu.name().toLowerCase());
			}
		}
	}

	public long finish(String name) {
		long result = System.currentTimeMillis() - start;
		logger.debug("Finishing %s took %d", name, result);
		start();
		return result;
	}
}
