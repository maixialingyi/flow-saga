package com.flow.saga.entity;

import java.lang.reflect.Method;

public class InvocationContext {

	private Class<?> targetClass;

	private Method method;

	private Class[] parameterTypes;


	public InvocationContext() {

	}

	public InvocationContext(Class<?> targetClass, Method method, Class[] parameterTypes) {
		this.method = method;
		this.parameterTypes = parameterTypes;
		this.targetClass = targetClass;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public Method getMethod() {
		return method;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

}
