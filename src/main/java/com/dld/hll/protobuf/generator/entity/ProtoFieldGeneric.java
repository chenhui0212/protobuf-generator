package com.dld.hll.protobuf.generator.entity;

import com.dld.hll.protobuf.generator.AssertUtil;
import com.dld.hll.protobuf.generator.ProtoUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Hui
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProtoFieldGeneric extends AbstractProtoInfo {
    // 1 -> Map<String, List<String>>
    // 2 -> List<List<String>>
    private ParameterizedType type;

    // 1 -> String.class
    // 2 -> null
    private List<Class<?>> paramClasses;

    private List<ProtoFieldType> protoFieldTypes;

    // 1 -> List<String>
    // 2 -> List<String>
    private ProtoFieldGeneric generic;

    public ProtoFieldGeneric(ParameterizedType type) {
        this.type = type;
    }

    public void addClass(Class<?> clazz) {
        if (paramClasses == null) {
            paramClasses = new ArrayList<>(2);
        }
        paramClasses.add(clazz);
    }

    public void addProtoFieldType(ProtoFieldType protoFieldType) {
        if (protoFieldTypes == null) {
            protoFieldTypes = new ArrayList<>(2);
        }
        protoFieldTypes.add(protoFieldType);
    }

    @Override
    public String getName() {
        return getTypeName().toLowerCase();
    }

    public boolean isCollection() {
        return Collection.class.isAssignableFrom((Class<?>) type.getRawType());
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom((Class<?>) type.getRawType());
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean hasDescription() {
        return false;
    }

    public String getProtoType() {
        StringBuilder buf = new StringBuilder();
        if (isCollection()) {
            buf.append("repeated ");
            if (paramClasses != null) {
                buf.append(ProtoUtil.getProtoType(paramClasses.get(0), protoFieldTypes.get(0)));
            } else {
                buf.append(generic.getTypeName());
            }
        } else if (isMap()) {
            buf.append("map<");
            AssertUtil.isTrue(paramClasses != null && paramClasses.size() > 0,
                    "generic field parse not right");
            if (paramClasses.size() == 1) {
                buf.append(ProtoUtil.getProtoType(paramClasses.get(0), protoFieldTypes.get(0)));
                buf.append(", ");
                buf.append(generic.getTypeName());
            } else {
                buf.append(ProtoUtil.getProtoType(paramClasses.get(0), protoFieldTypes.get(0)));
                buf.append(", ");
                buf.append(ProtoUtil.getProtoType(paramClasses.get(1), protoFieldTypes.get(1)));
            }
            buf.append(">");
        }
        return buf.toString();
    }

    public String getTypeName() {
        StringBuilder buf = new StringBuilder();
        if (isCollection()) {
            buf.append("List");
            if (paramClasses != null) {
                buf.append(paramClasses.get(0).getSimpleName());
            } else {
                buf.append(generic.getTypeName());
            }
        } else if (isMap()) {
            buf.append("Map");
            AssertUtil.isTrue(paramClasses != null && paramClasses.size() > 0,
                    "generic field parse not right");
            if (paramClasses.size() == 1) {
                buf.append(paramClasses.get(0).getSimpleName());
                buf.append(generic.getTypeName());
            } else {
                buf.append(paramClasses.get(0).getSimpleName());
                buf.append(paramClasses.get(1).getSimpleName());
            }
        }
        return buf.toString();
    }
}
