package com.wesleyhome.processor;

import javax.annotation.processing.ProcessingEnvironment;

public class Environment {
	private static ThreadLocal<ProcessingEnvironment> environment = new ThreadLocal<ProcessingEnvironment>();

	/**
	 * Return the APT environment.
	 *
	 * @return the APT environment or <code>null</code> if none have been set.
	 */
	public static ProcessingEnvironment getEnvironment() {
		return environment.get();
	}

	public static void setEnvironment(final ProcessingEnvironment env) {
		environment.set(env);
	}
}
