package com.dld.hll.protobuf.generator.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Method;

/**
 * @author Chen Hui
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProtoMethod extends AbstractProtoInfo {
    private Method method;

    /**
     * 方法参数个数最多一个
     */
    private Class<?> parameterType;
    private ProtoObject parameterProtoObject;

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
    public String getDescription() {
        return getDescription(method);
    }

    @Override
    public boolean hasDescription() {
        return hasDescription(method);
    }

    public String getParameterTypeName() {
        if (parameterType == null) {
            return "Empty";
        }
        return parameterType.getSimpleName();
    }

    public String getReturnTypeName() {
        if (returnType == null) {
            return "Empty";
        }
        return returnType.getSimpleName();
    }
}
