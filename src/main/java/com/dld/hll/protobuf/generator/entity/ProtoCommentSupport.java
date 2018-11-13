package com.dld.hll.protobuf.generator.entity;

import com.dld.hll.protobuf.generator.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Chen Hui
 */
public abstract class ProtoCommentSupport implements ProtoInfo {

    /**
     * 注释使用的注解类型
     */
    public static Class<? extends Annotation> commentType;

    /**
     * 获取注解信息的方法
     */
    public static Method valueMethod;

    /**
     * 获取注释
     */
    public abstract String getComment();


    /**
     * 获取指定元素的注释值
     */
    String getComment(AnnotatedElement element) {
        if (commentType != null) {
            Annotation annotation = element.getAnnotation(commentType);
            if (annotation != null) {
                try {
                    String comment = (String) valueMethod.invoke(annotation);
                    return StringUtils.hasText(comment) ? comment : null;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
            return null;
        }
        return null;
    }
}
