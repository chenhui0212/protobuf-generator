package com.dld.hll.protobuf.generator.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * @author Chen Hui
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProtoField extends AbstractProtoInfo {
    private Class<?> clazz;
    private Field field;
    private ProtoFieldType protoFieldType;

    /**
     * Collection<?> 和 Map<?, Collection<?>>
     * 只处理集合类型的范型，Map中的不能出现范型类型
     */
    private ProtoFieldGeneric generic;

    public ProtoField(Field field) {
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getDescription() {
        return getDescription(field);
    }

    @Override
    public boolean hasDescription() {
        return hasDescription(field);
    }

    public boolean isCollection() {
        return Collection.class.isAssignableFrom(field.getType());
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(field.getType());
    }

    public String getTypeName() {
        if (clazz != null) {
            if (protoFieldType == ProtoFieldType.OBJECT || protoFieldType == ProtoFieldType.ENUM) {
                return clazz.getSimpleName();
            } else {
                return protoFieldType.getName();
            }
        } else if (generic != null) {
            return generic.getTypeName();
        }
        return null;
    }
}
