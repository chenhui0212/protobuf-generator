package com.dld.hll.protobuf.generator.entity;

import com.dld.hll.protobuf.generator.util.ProtoUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

/**
 * @author Chen Hui
 */
@Getter
@Setter
public class ProtoField extends ProtoCommentSupport {

    private Field field;

    /**
     * 字段类型
     */
    private Class<?> fieldType;
    private ProtoFieldType protoFieldType;

    /**
     * 字段为对象类型
     */
    private ProtoObject protoObject;

    /**
     * 字段为范型类型
     */
    private ProtoGenericField generic;


    public ProtoField(Field field) {
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getComment() {
        return getComment(field);
    }

    /**
     * 字段类型名称
     */
    public String getTypeName() {
        if (isNotGeneric()) {
            if (ProtoUtils.isBasicType(protoFieldType)) {
                return protoFieldType.getName();
            } else {
                return fieldType.getSimpleName();
            }
        } else {
            // 泛型没有类型名，在生成文件时再做处理
            return null;
        }
    }

    private boolean isNotGeneric() {
        return generic == null;
    }
}
