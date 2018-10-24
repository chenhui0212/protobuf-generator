package com.dld.hll.protobuf.generator.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * @author Chen Hui
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProtoService extends AbstractProtoInfo {
    private Class<?> clazz;

    /**
     * 继承接口（暂时未使用）
     */
    private Class<?>[] interfaces;

    /**
     * 解析后的方法
     */
    private List<ProtoMethod> protoMethods;

    /**
     * 全部需要生成的对象（剔除父类中的，当前类已经存在的，以及与其它接口类中定义的）
     */
    private Map<Class<?>, ProtoObject> protoObjectMap;

    public ProtoService(Class<?> clazz) {
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
