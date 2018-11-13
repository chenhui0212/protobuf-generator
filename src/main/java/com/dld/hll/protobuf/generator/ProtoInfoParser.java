package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.*;
import com.dld.hll.protobuf.generator.util.AssertUtils;
import com.dld.hll.protobuf.generator.util.ProtoUtils;
import lombok.AllArgsConstructor;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Hui
 */
@AllArgsConstructor
public class ProtoInfoParser {

    private ProtoInfoRegistry registry;


    /**
     * 解析服务接口类
     * 不考虑存在继承类的情况
     */
    public void parseService(Class<?> serviceClass) {
        // 跳过未定义任何方法的接口类
        Method[] methods = serviceClass.getDeclaredMethods();
        if (methods.length == 0) {
            return;
        }

        // 提前注册解析后的接口类，以便后续可以查询到当前解析的服务接口
        ProtoService protoService = new ProtoService(serviceClass);
        registry.register(protoService);

        // 依次解析接口类中全部方法
        List<ProtoMethod> protoMethods = new ArrayList<>(methods.length);
        for (Method method : methods) {
            ProtoMethod protoMethod = parseMethod(method);
            protoMethods.add(protoMethod);
        }
        protoService.setProtoMethods(protoMethods);
    }

    /**
     * 解析类中方法
     */
    public ProtoMethod parseMethod(Method method) {
        // 获取方法参数，判断参数个数至多为一个
        Class<?>[] parameterTypes = method.getParameterTypes();
        AssertUtils.isTrue(parameterTypes.length <= 1,
                "More then one parameter in the method [" + method.getName() + "] of service [" +
                        method.getDeclaringClass().getSimpleName() + "].");

        // 解析方法参数类型
        ProtoMethod protoMethod = new ProtoMethod(method);
        if (parameterTypes.length == 1) {
            Class<?> parameterType = parameterTypes[0];
            protoMethod.setParameterType(parameterType);
            ProtoObject parameterPO = parseObject(parameterType);
            protoMethod.setParameterProtoObject(parameterPO);
        }

        // 解析方法返回类型
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            protoMethod.setReturnType(returnType);
            ProtoObject returnPO = parseObject(returnType);
            protoMethod.setReturnProtoObject(returnPO);
        }
        return protoMethod;
    }

    public ProtoObject parseObject(Class<?> clazz) {
        return parseObjectOrEnum(clazz, ProtoFieldType.OBJECT);
    }

    public ProtoObject parseObjectOrEnum(Class<?> clazz, ProtoFieldType pfType) {
        ProtoObject protoObject = registry.getCache(clazz);
        if (protoObject != null) {
            registry.increaseObjectCitations(protoObject);
            return protoObject;
        }

        registry.addParsing(clazz);
        if (pfType == ProtoFieldType.OBJECT) {
            protoObject = doParseObject(clazz);
        } else if (pfType == ProtoFieldType.ENUM) {
            protoObject = doParseEnum(clazz);
        } else {
            throw new RuntimeException("PFType must be object or enum");
        }
        registry.putCache(clazz, protoObject);
        registry.removeParsing(clazz);

        return protoObject;
    }

    /**
     * 解析接口中的类
     */
    public ProtoObject doParseObject(Class<?> clazz) {
        ProtoObject po = new ProtoObject(clazz);

        // 解析父类
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            ProtoObject superPO = parseObject(superClass);
            po.setSuperProtoObject(superPO);
        }

        // 解析类字段
        Field[] declaredFields = clazz.getDeclaredFields();
        List<ProtoField> pfs = new ArrayList<>(declaredFields.length);
        for (Field field : clazz.getDeclaredFields()) {
            ProtoField pf = parseField(field);
            pfs.add(pf);
        }
        po.setProtoFields(pfs);

        return po;
    }

    /**
     * 解析枚举类
     */
    public ProtoObject doParseEnum(Class<?> clazz) {
        ProtoObject po = new ProtoObject(clazz);

        // 解析枚举字段
        Field[] fields = clazz.getFields();
        List<ProtoField> pfs = new ArrayList<>(fields.length);
        for (Field field : fields) {
            // 枚举的字段无需解析
            pfs.add(new ProtoField(field));
        }
        po.setProtoFields(pfs);
        return po;
    }

    /**
     * 解析类字段
     */
    private ProtoField parseField(Field field) {
        ProtoField protoField = new ProtoField(field);
        Class<?> fieldType = field.getType();
        protoField.setFieldType(fieldType);

        // 解析字段类型，或其泛型类型
        if (ProtoUtils.isNotGeneric(fieldType)) {
            // 解析字段类型
            ProtoFieldType pfType = ProtoUtils.getProtoFieldType(fieldType);
            protoField.setProtoFieldType(pfType);

            // 解析非基础类型字段
            if (pfType == ProtoFieldType.OBJECT || pfType == ProtoFieldType.ENUM) {
                // 如果解析类已经在解析了，则跳过解析（避免循环解析）
                if (registry.isNotParsing(fieldType)) {
                    ProtoObject fieldPO = parseObjectOrEnum(fieldType, pfType);
                    protoField.setProtoObject(fieldPO);
                }
            }
        } else {
            // 解析范型，如果已经在解析了，则跳过
            if (registry.isNotParsing(field.getGenericType())) {
                ProtoGenericField protoFieldGeneric = parseFieldGeneric(field, field.getGenericType(), true);
                protoField.setGeneric(protoFieldGeneric);
            } else {
                protoField.setGeneric(registry.getParsingType().get(field.getGenericType()));
            }
        }
        return protoField;
    }

    /**
     * 解析范型（缓存）
     *
     * @param isOutermost 是否是最外层泛型解析
     */
    private ProtoGenericField parseFieldGeneric(Field field, Type type, boolean isOutermost) {
        ProtoGenericField genericField = registry.getCache(type);
        if (genericField != null) {
            registry.increaseGenericFieldCitations(genericField, isOutermost);
            return genericField;
        }

        genericField = doParseFieldGeneric(field, type, isOutermost);
        registry.putCache(type, genericField);
        registry.removeParsing(type);
        return genericField;
    }

    /**
     * 解析范型
     */
    private ProtoGenericField doParseFieldGeneric(Field field, Type type, boolean isOutermost) {
        // No:  List list = ...;  or  Map map = ...;
        if (!(type instanceof ParameterizedType)) {
            throw new RuntimeException("No nestedGeneric parameter type is specified in Field[" +
                    field.getName() + "] of Class[" + field.getDeclaringClass().getName() + "].");
        }

        ProtoGenericField generic = new ProtoGenericField(field, (ParameterizedType) type, isOutermost ? 0 : 1);
        registry.addParsing(type, generic);
        Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
        for (Type paramType : paramTypes) {
            // No:  List<?> list = ...;  or  Map<?, ?> map = ...;
            if (paramType instanceof WildcardType) {
                throw new RuntimeException("No nestedGeneric parameter type is specified in Field[" +
                        field.getName() + "] of Class[" + field.getDeclaringClass().getName() + "].");
            }

            // 解析非泛型
            if (paramType instanceof Class) {
                Class<?> paramClass = (Class<?>) paramType;
                generic.addParameterType(paramClass);
                ProtoFieldType pfType = ProtoUtils.getProtoFieldType(paramClass);
                generic.addProtoFieldType(pfType);

                // 如果是非一般数据类型，则进一步解析
                if (pfType == ProtoFieldType.ENUM || pfType == ProtoFieldType.OBJECT) {
                    // 如果解析类已经在解析了，则跳过解析（避免循环解析）
                    if (registry.isNotParsing(paramClass)) {
                        ProtoObject po = parseObjectOrEnum(paramClass, pfType);
                        generic.addProtoObject(po);
                    }
                }
            }
            // 解析嵌套范型
            else {
                // 如果已经在解析了，则跳过解析
                if (registry.isNotParsing(paramType)) {
                    ProtoGenericField protoFieldGeneric = parseFieldGeneric(field, paramType, false);
                    generic.setNestedGeneric(protoFieldGeneric);
                } else {
                    ProtoGenericField nestedGeneric = registry.getParsingType().get(paramType);
                    generic.setNestedGeneric(nestedGeneric);
                    registry.increaseParsingGenericCitations(nestedGeneric);
                }
            }
        }
        return generic;
    }
}
