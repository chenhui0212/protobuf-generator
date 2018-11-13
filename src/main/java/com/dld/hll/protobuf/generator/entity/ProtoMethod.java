package com.dld.hll.protobuf.generator.entity;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * @author Chen Hui
 */
@Getter
@Setter
public class ProtoMethod extends ProtoCommentSupport {

    private Method method;

    /**
     * 方法参数类型，个数最多一个
     */
    private Class<?> parameterType;
    private ProtoObject parameterProtoObject;

    /**
     * 返回值类型
     */
    private Class<?> returnType;
    private ProtoObject returnProtoObject;


    public ProtoMethod(Method method) {
        this.method = method;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public String getComment() {
        return getComment(method);
    }

    /**
     * 获取方法参数类型名称
     */
    public String getParameterTypeName() {
        if (parameterType == null) {
            return "Empty";
        }
        return parameterType.getSimpleName();
    }

    /**
     * 获取方法返回值类型名称
     */
    public String getReturnTypeName() {
        if (returnType == null) {
            return "Empty";
        }
        return returnType.getSimpleName();
    }
}
