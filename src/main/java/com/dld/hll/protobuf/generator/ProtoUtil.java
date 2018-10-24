package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.ProtoFieldType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Chen Hui
 */
public class ProtoUtil {
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

    public static String getProtoType(Class<?> clazz, ProtoFieldType protoFieldType) {
        if (protoFieldType == ProtoFieldType.OBJECT || protoFieldType == ProtoFieldType.ENUM) {
            return clazz.getSimpleName();
        } else {
            return protoFieldType.getName();
        }
    }
}
