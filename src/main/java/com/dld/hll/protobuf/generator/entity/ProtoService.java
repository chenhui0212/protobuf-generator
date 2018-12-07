package com.dld.hll.protobuf.generator.entity;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Hui
 */
@Getter
@Setter
public class ProtoService extends ProtoCommentSupport {

    private Class<?> serviceClass;

    /**
     * 全部方法
     */
    private List<ProtoMethod> protoMethods;

    /**
     * 当前服务接口中全部解析类
     */
    private Map<Class<?>, ProtoObject> protoObjectMap = new HashMap<>();

    /**
     * 当前服务接口中全部内嵌泛型类
     */
    private Map<Type, ProtoGenericField> genericFieldMap = new HashMap<>();


    public ProtoService(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    @Override
    public String getName() {
        return serviceClass.getSimpleName();
    }

    @Override
    public String getComment() {
        return getComment(serviceClass);
    }

    public boolean isExists(Class<?> clazz) {
        return protoObjectMap.containsKey(clazz);
    }

    public boolean isExists(Type type) {
        return genericFieldMap.containsKey(type);
    }

    public void addParsed(Class<?> clazz, ProtoObject protoObject) {
        protoObjectMap.put(clazz, protoObject);
    }

    public void addParsed(Type type, ProtoGenericField protoGenericField) {
        genericFieldMap.put(type, protoGenericField);
    }
}
