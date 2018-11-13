package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.ProtoGenericField;
import com.dld.hll.protobuf.generator.entity.ProtoObject;
import com.dld.hll.protobuf.generator.entity.ProtoService;
import com.dld.hll.protobuf.generator.util.AssertUtils;
import com.dld.hll.protobuf.generator.util.ProtoUtils;
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

    private List<ProtoService> protoServices = new ArrayList<>();

    /**
     * 所有已经解析的类
     */
    private Map<Class<?>, ProtoObject> protoObjectMap = new HashMap<>();

    /**
     * 正在解析的类
     */
    private Set<Class<?>> parsingClass = new HashSet<>();

    /**
     * 所有已经解析的泛型类
     */
    private Map<Type, ProtoGenericField> genericFieldMap = new HashMap<>();

    /**
     * 正在解析的泛型
     */
    private Map<Type, ProtoGenericField> parsingType = new HashMap<>();


    public void register(ProtoService protoService) {
        AssertUtils.notNull(protoService);
        protoServices.add(protoService);
    }

    public ProtoObject getCache(Class<?> clazz) {
        return protoObjectMap.get(clazz);
    }

    public void putCache(Class<?> clazz, ProtoObject obj) {
        protoObjectMap.put(clazz, obj);
        getCurrentService().getProtoObjectMap().put(clazz, obj);
    }

    public ProtoGenericField getCache(Type type) {
        return genericFieldMap.get(type);
    }

    public void putCache(Type type, ProtoGenericField genericField) {
        genericFieldMap.put(type, genericField);
        getCurrentService().getGenericFieldMap().put(type, genericField);
    }

    public void addParsing(Class<?> clazz) {
        parsingClass.add(clazz);
    }

    public void removeParsing(Class<?> clazz) {
        parsingClass.remove(clazz);
    }

    public void addParsing(Type type, ProtoGenericField genericField) {
        parsingType.put(type, genericField);
    }

    public void removeParsing(Type type) {
        parsingType.remove(type);
    }

    public boolean isNotParsing(Class<?> clazz) {
        return !parsingClass.contains(clazz);
    }

    public boolean isNotParsing(Type type) {
        return !parsingType.containsKey(type);
    }

    /**
     * 增加对象引用次数
     */
    public void increaseObjectCitations(ProtoObject protoObject) {
        if (!isExistsInCurrentService(protoObject.getClazz())) {
            ProtoUtils.increaseCitations(protoObject);
        }
    }

    /**
     * 增加泛型引用次数
     *
     * @param isOutermost 是否是最外层泛型
     */
    public void increaseGenericFieldCitations(ProtoGenericField genericField, boolean isOutermost) {
        // 泛型是否为最外层（字段第一次解析）
        if (isOutermost) {
            // 最外层，且缓存不存在于当前解析Service中，则增加泛型内嵌泛型或者类引用次数
            if (!isExistsInCurrentService(genericField.getType())) {
                for (ProtoObject protoObject : genericField.getProtoObjects()) {
                    ProtoUtils.increaseCitations(protoObject);
                }
                if (genericField.getNestedGeneric() != null) {
                    ProtoUtils.increaseCitations(genericField.getNestedGeneric());
                }
            }
        } else {
            // 缓存是否存在于当前解析Service
            if (isExistsInCurrentService(genericField.getType())) {
                // 存在，且泛型之前是最外层，则设置泛型为内层
                if (genericField.getCitations() == 0) {
                    genericField.setCitations(1);
                }
            } else {
                // 不存在，则泛型类及其内嵌泛型或者类引用次数均需要增加
                ProtoUtils.increaseCitations(genericField);
            }
        }
    }

    /**
     * 增加正在解析泛型的引用次数
     */
    public void increaseParsingGenericCitations(ProtoGenericField genericField) {
        // 缓存是否存在于当前解析Service
        if (isExistsInCurrentService(genericField.getType())) {
            // 存在，且泛型之前是最外层，则设置泛型为内层
            if (genericField.getCitations() == 0) {
                genericField.setCitations(1);
            }
        } else {
            // 不存在，则泛型类及其内嵌泛型或者类引用次数均需要增加
            ProtoUtils.increaseCitations(genericField);
        }
    }

    /**
     * 输入类型是否缓存于当前解析Service中
     */
    private boolean isExistsInCurrentService(Class<?> clazz) {
        return getCurrentService().getProtoObjectMap().containsKey(clazz) || parsingClass.contains(clazz);
    }

    /**
     * 输入类型是否缓存于当前解析Service中
     */
    private boolean isExistsInCurrentService(Type type) {
        return getCurrentService().getGenericFieldMap().containsKey(type) || parsingType.containsKey(type);
    }

    private ProtoService getCurrentService() {
        return protoServices.get(protoServices.size() - 1);
    }
}
