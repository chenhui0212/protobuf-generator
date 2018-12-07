package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.ProtoGenericField;
import com.dld.hll.protobuf.generator.entity.ProtoObject;
import com.dld.hll.protobuf.generator.entity.ProtoService;
import com.dld.hll.protobuf.generator.util.AssertUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Hui
 */
@Getter
@Setter
public class ProtoInfoRegistry {

    private List<ProtoService> protoServices = new ArrayList<>();

    /**
     * 所有已经解析的类
     */
    private Map<Class<?>, ProtoObject> protoObjectMap = new HashMap<>();

    /**
     * 正在解析的类
     */
    private Map<Class<?>, ProtoObject> parsingClass = new HashMap<>();

    /**
     * 所有已经解析的泛型类
     */
    private Map<Type, ProtoGenericField> genericFieldMap = new HashMap<>();

    /**
     * 正在解析的泛型
     */
    private Map<Type, ProtoGenericField> parsingType = new HashMap<>();


    public void register(ProtoService service) {
        AssertUtils.notNull(service);
        protoServices.add(service);
    }

    public void addCache(Class<?> clazz, ProtoObject obj) {
        protoObjectMap.put(clazz, obj);
    }

    public void addCache(Type type, ProtoGenericField genericField) {
        genericFieldMap.put(type, genericField);
    }

    public ProtoObject getCache(Class<?> clazz) {
        return protoObjectMap.get(clazz);
    }

    public ProtoGenericField getCache(Type type) {
        return genericFieldMap.get(type);
    }

    public void addParsing(Class<?> clazz, ProtoObject protoObject) {
        parsingClass.put(clazz, protoObject);
    }

    public void addParsing(Type type, ProtoGenericField genericField) {
        parsingType.put(type, genericField);
    }

    public void removeParsing(Class<?> clazz) {
        parsingClass.remove(clazz);
    }

    public void removeParsing(Type type) {
        parsingType.remove(type);
    }

    public boolean isParsing(Class<?> clazz) {
        return parsingClass.containsKey(clazz);
    }

    public boolean isParsing(Type type) {
        return parsingType.containsKey(type);
    }

    public ProtoObject getParsing(Class<?> clazz) {
        return parsingClass.get(clazz);
    }

    public ProtoGenericField getParsing(Type type) {
        return parsingType.get(type);
    }

    public ProtoObject getFormParsingOrCache(Class<?> clazz) {
        return isParsing(clazz) ? getParsing(clazz) : getCache(clazz);
    }
}
