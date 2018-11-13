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

    private Class<?> clazz;

    /**
     * 全部方法
     */
    private List<ProtoMethod> protoMethods;

    /**
     * 当前服务接口中全部解析类（不包括其它服务已解析的）
     */
    private Map<Class<?>, ProtoObject> protoObjectMap = new HashMap<>();

    /**
     * 当前服务接口中全部解析泛型类（不包括其它服务已解析的）
     */
    private Map<Type, ProtoGenericField> genericFieldMap = new HashMap<>();


    public ProtoService(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return clazz.getSimpleName();
    }

    @Override
    public String getComment() {
        return getComment(clazz);
    }
}
