package com.dld.hll.protobuf.generator.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Chen Hui
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProtoObject extends AbstractProtoInfo {
    private Class<?> clazz;

    /**
     * 父类
     */
    ProtoObject superProtoObject;

    /**
     * 全部字段
     */
    List<ProtoField> protoFields;

    /**
     * 对象类型字段
     */
    List<ProtoObject> protoFieldObjects;

    public ProtoObject(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return clazz.getSimpleName();
    }

    @Override
    public String getDescription() {
        return getDescription(clazz);
    }

    @Override
    public boolean hasDescription() {
        return hasDescription(clazz);
    }
}
