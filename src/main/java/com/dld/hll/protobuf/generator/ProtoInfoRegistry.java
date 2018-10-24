package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.ProtoFieldGeneric;
import com.dld.hll.protobuf.generator.entity.ProtoObject;
import com.dld.hll.protobuf.generator.entity.ProtoService;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Chen Hui
 */
@Getter
@Setter
public class ProtoInfoRegistry {

    /**
     * 全部解析接口
     */
    private List<ProtoService> protoServices = new ArrayList<>();

    /**
     * 缓存接口中类
     */
    private Map<Class<?>, ProtoObject> servicePOs = new HashMap<>();

    /**
     * 缓存接口中类共同的类
     * 比如父类，共同的VO类...
     */
    private Map<Class<?>, ProtoObject> commonPOs = new HashMap<>();

    /**
     * 缓存范型类型
     */
    private Map<Type, ProtoFieldGeneric> fieldGenerics = new HashMap<>();

    public void register(ProtoService protoService) {
        AssertUtil.notNull(protoService);
        protoServices.add(protoService);
    }

    /**
     * 获取对应的缓存
     *
     * @param isCommon 是否是共同
     */
    public Map<Class<?>, ProtoObject> getCache(boolean isCommon) {
        if (isCommon) {
            return commonPOs;
        } else {
            return servicePOs;
        }
    }
}
