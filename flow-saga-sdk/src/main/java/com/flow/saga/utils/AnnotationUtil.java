package com.flow.saga.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtil {
    public static <A extends Annotation> A findAnnotation(ProceedingJoinPoint joinPoint, Class<A> annotationType)
            throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        A annotation = AnnotationUtils.findAnnotation(method, annotationType);
        if (annotation != null) {
            return annotation;
        }
        Method targetMethod = AopUtils.getTargetClass(joinPoint.getTarget()).getDeclaredMethod(method.getName(),
                method.getParameterTypes());
        annotation = AnnotationUtils.findAnnotation(targetMethod, annotationType);
        return annotation;
    }

}
