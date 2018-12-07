package com.dld.hll.protobuf.generator.util;

import com.dld.hll.protobuf.generator.entity.ProtoField;
import com.dld.hll.protobuf.generator.entity.ProtoFieldType;
import com.dld.hll.protobuf.generator.entity.ProtoGenericField;
import com.dld.hll.protobuf.generator.entity.ProtoObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Hui
 */
public abstract class ProtoUtils {

    /**
     * 已增加引用次数的ProtoObject和ProtoGenericField列表
     * 避免引用循环
     */
    private static Set<ProtoObject> objectIncreased = new HashSet<>();
    private static Set<ProtoGenericField> genericIncreased = new HashSet<>();


    public static ProtoFieldType getProtoFieldType(Class<?> clazz) {
        if (double.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.DOUBLE;
        } else if (Double.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.DOUBLE_VALUE;
        } else if (float.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.FLOAT;
        } else if (Float.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.FLOAT_VALUE;
        } else if (long.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.LONG;
        } else if (Long.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.LONG_VALUE;
        } else if (int.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz) ||
                byte.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.INT;
        } else if (Integer.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz) ||
                Byte.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.INT_VALUE;
        } else if (boolean.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.BOOL;
        } else if (Boolean.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.BOOL_VALUE;
        } else if (String.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.STRING_VALUE;
        } else if (BigDecimal.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.BIG_DECIMAL;
        } else if (LocalDate.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.LOCAL_DATE;
        } else if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.LOCAL_DATETIME;
        } else if (LocalTime.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.LOCAL_TIME;
        } else if (Timestamp.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.TIMESTAMP;
        } else if (Enum.class.isAssignableFrom(clazz)) {
            return ProtoFieldType.ENUM;
        } else {
            return ProtoFieldType.OBJECT;
        }
    }

    public static String getTypeName(Class<?> clazz, ProtoFieldType protoFieldType) {
        if (protoFieldType == ProtoFieldType.OBJECT || protoFieldType == ProtoFieldType.ENUM) {
            return clazz.getSimpleName();
        } else {
            return protoFieldType.getName();
        }
    }

    public static boolean isBasicType(ProtoFieldType protoFieldType) {
        return protoFieldType != ProtoFieldType.OBJECT && protoFieldType != ProtoFieldType.ENUM;
    }

    public static boolean isNotGeneric(Class<?> clazz) {
        return !isCollection(clazz) && !isMap(clazz);
    }

    public static boolean isCollection(Type type) {
        return Collection.class.isAssignableFrom((Class<?>) type);
    }

    public static boolean isMap(Type type) {
        return Map.class.isAssignableFrom((Class<?>) type);
    }

    /**
     * 清空已增加引用次数的缓存，并增加ProtoObject对象引用次数
     */
    public static void increaseCitations(ProtoObject object) {
        objectIncreased.clear();
        genericIncreased.clear();
        doIncreaseCitations(object);
    }

    /**
     * 增加ProtoObject对象，及其内部所有的ProtoObject和ProtoGenericField的引用次数
     */
    private static void doIncreaseCitations(ProtoObject object) {
        if (objectIncreased.contains(object)) {
            return;
        }

        object.increaseCitations();
        objectIncreased.add(object);

        if (object.getSuperProtoObject() != null) {
            doIncreaseCitations(object.getSuperProtoObject());
        }

        for (ProtoField field : object.getProtoFields()) {
            if (field.getProtoObject() != null) {
                doIncreaseCitations(field.getProtoObject());
            }

            if (field.getGeneric() != null) {
                doIncreaseCitations(field.getGeneric());
            }
        }
    }

    /**
     * 清空已增加引用次数的缓存，并增加ProtoGenericField对象引用次数
     */
    public static void increaseCitations(ProtoGenericField genericField) {
        objectIncreased.clear();
        genericIncreased.clear();
        doIncreaseCitations(genericField);
    }

    /**
     * 增加ProtoGenericField对象，及其内嵌所有的ProtoObject和ProtoGenericField的引用次数
     */
    private static void doIncreaseCitations(ProtoGenericField genericField) {
        if (genericIncreased.contains(genericField)) {
            return;
        }

        genericField.increaseCitations();
        genericIncreased.add(genericField);

        for (ProtoObject protoObject : genericField.getProtoObjects()) {
            doIncreaseCitations(protoObject);
        }

        if (genericField.getNestedGeneric() != null) {
            doIncreaseCitations(genericField.getNestedGeneric());
        }
    }
}
