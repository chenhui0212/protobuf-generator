package com.dld.hll.protobuf.generator.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Chen Hui
 */
public abstract class AbstractProtoInfo implements ProtoInfo {
    /**
     * 指定注释使用的注解类型
     */
    public static Class<? extends Annotation> common;

    /**
     * 获取注解信息对应的方法
     */
    public static Method method;

    String getDescription(AnnotatedElement element) {
        if (common != null) {
            Annotation annotation = element.getAnnotation(common);
            if (annotation != null) {
                try {
                    return "// " + method.invoke(annotation);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        return null;
    }

    boolean hasDescription(AnnotatedElement element) {
        if (common != null) {
            return element.getAnnotation(common) != null;
        }
        return false;
    }
}
