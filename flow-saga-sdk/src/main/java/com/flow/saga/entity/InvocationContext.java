package com.flow.saga.entity;

import java.lang.reflect.Method;

/**
 * @author songzeqi
 *
 * 方法调用的封装，可以对方法调用的持久化
 */
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
