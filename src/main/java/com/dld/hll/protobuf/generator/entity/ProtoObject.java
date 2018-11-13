package com.dld.hll.protobuf.generator.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 对应 ProtoFieldType 中 {@link ProtoFieldType#ENUM} 和 OBJECT{@link ProtoFieldType#OBJECT} 类型
 *
 * @author Chen Hui
 */
@Getter
@Setter
public class ProtoObject extends ProtoCommentSupport {

    private Class<?> clazz;

    /**
     * 被引用次数
     */
    private int citations = 1;

    /**
     * 父类
     */
    ProtoObject superProtoObject;

    /**
     * 全部字段
     */
    List<ProtoField> protoFields;


    public ProtoObject(Class<?> clazz) {
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

    public void increaseCitations() {
        citations++;
    }
}
