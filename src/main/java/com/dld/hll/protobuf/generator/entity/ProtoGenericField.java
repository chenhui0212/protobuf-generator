package com.dld.hll.protobuf.generator.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * 目前只支持 Collection 和 Map 类型的泛型，且 Map 的 Key 只能为一般数据类型
 *
 * @author Chen Hui
 */
@Getter
@Setter
@NoArgsConstructor
public class ProtoGenericField extends ProtoCommentSupport {

    /**
     * 所属的字段（用此查询所属的类）
     */
    private Field field;

    /**
     * 泛型类型（java.util.List<java.util.String>）
     */
    private ParameterizedType type;

    /**
     * 内嵌引用次数（非最外层）
     * 外层初始化 0，内嵌初始化 1
     */
    private int citations;

    /**
     * 泛型参数个数（List有一个，Map有两个）
     */
    private int length;

    /**
     * 泛型参数对应的Class类型
     */
    private List<Class<?>> parameterTypes;

    /**
     * parameterTypes 对应的 ProtoFieldType
     */
    private List<ProtoFieldType> protoFieldTypes;

    /**
     * 内嵌对象类型
     */
    private List<ProtoObject> protoObjects;

    /**
     * 内嵌泛型类型（有且只支持一个。Map类型时，Key不能为泛型）
     * 注意：解析时对不满足条件的情况未做判断及抛出异常
     */
    private ProtoGenericField nestedGeneric;


    public ProtoGenericField(Field field, ParameterizedType type, int citations) {
        this.field = field;
        this.type = type;
        this.citations = citations;
        this.length = type.getActualTypeArguments().length;
        parameterTypes = new ArrayList<>(length);
        protoFieldTypes = new ArrayList<>(length);
        protoObjects = new ArrayList<>(length);
    }

    public void addParameterType(Class<?> clazz) {
        parameterTypes.add(clazz);
    }

    public void addProtoFieldType(ProtoFieldType protoFieldType) {
        protoFieldTypes.add(protoFieldType);
    }

    public void addProtoObject(ProtoObject protoObject) {
        protoObjects.add(protoObject);
    }

    @Override
    public String getName() {
        return getTypeName().toLowerCase();
    }

    @Override
    public String getComment() {
        return null;
    }

    /**
     * 内嵌的范型需要转换为 proto message 进行封装，该方法返回该 message 名称
     */
    public String getTypeName() {
        StringBuilder typeName = new StringBuilder(((Class<?>) type.getRawType()).getSimpleName());
        if (isNotGeneric()) {
            // 不存在内嵌泛型时，拼接全部的参数类型
            for (Class<?> type : parameterTypes) {
                typeName.append(type.getSimpleName());
            }
        } else {
            // 存在内嵌泛型
            if (length == 1) {
                typeName.append(nestedGeneric.getTypeName());
            } else {
                typeName.append(parameterTypes.get(0).getSimpleName())
                        .append(nestedGeneric.getTypeName());
            }
        }
        return typeName.toString();
    }

    public boolean isNotGeneric() {
        return nestedGeneric == null;
    }

    public void increaseCitations() {
        citations++;
    }
}
