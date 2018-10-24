package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.*;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Hui
 */
@AllArgsConstructor
public class ProtoInfoParser {

    private ProtoInfoRegistry registry;

    /**
     * 解析服务接口类
     *
     * @param serviceClass 接口类
     */
    public void parseService(Class<?> serviceClass) {
        Method[] methods = serviceClass.getDeclaredMethods();
        if (methods == null) {
            return;
        }
        ProtoService protoService = new ProtoService(serviceClass);

        // 解析类中全部的方法
        List<ProtoMethod> protoMethods = new ArrayList<>(methods.length);
        for (Method method : methods) {
            protoMethods.add(parseMethod(method));
        }

        protoService.setProtoMethods(protoMethods);
        registry.register(protoService);
    }

    /**
     * 解析类中方法
     */
    public ProtoMethod parseMethod(Method method) {
        // 获取方法参数，判断参数个数至多为一个
        Class<?>[] parameterTypes = method.getParameterTypes();
        AssertUtil.isTrue(parameterTypes.length < 2,
                "More then one parameter in the method [" + method.getName() + "] of service [" +
                        method.getDeclaringClass().getSimpleName() + "].");

        // 解析方法参数类型
        ProtoMethod protoMethod = new ProtoMethod(method);
        if (parameterTypes.length == 1) {
            Class<?> parameterType = parameterTypes[0];
            protoMethod.setParameterType(parameterType);
            ProtoObject parameterPO = parseObject(parameterType, false);
            protoMethod.setParameterProtoObject(parameterPO);
        }

        // 解析方法返回类型
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            protoMethod.setReturnType(returnType);
            ProtoObject returnPO = parseObject(returnType, false);
            protoMethod.setReturnProtoObject(returnPO);
        }
        return protoMethod;
    }

    public ProtoObject parseObject(Class<?> clazz, boolean isCommon) {
        return parseObjectOrEnum(clazz, isCommon, ProtoFieldType.OBJECT);
    }

    public ProtoObject parseObjectOrEnum(Class<?> clazz, boolean isCommon, ProtoFieldType pfType) {
        Map<Class<?>, ProtoObject> cache = registry.getCache(isCommon);
        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        }

        ProtoObject po;
        if (pfType == ProtoFieldType.OBJECT) {
            po = doParseObject(clazz, isCommon);
        } else if (pfType == ProtoFieldType.ENUM) {
            po = doParseEnum(clazz);
        } else {
            throw new RuntimeException("pfType must be object or enum");
        }
        cache.put(clazz, po);
        return po;
    }

    /**
     * 解析接口中的类
     */
    public ProtoObject doParseObject(Class<?> clazz, boolean isCommon) {
        ProtoObject po = new ProtoObject(clazz);
        List<ProtoField> pfs = new ArrayList<>();
        List<ProtoObject> fieldPOs = new ArrayList<>();

        // 解析父类
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            ProtoObject superPO = parseObject(superClass, true);

            // 添加全部父类中的字段
            pfs.addAll(superPO.getProtoFields());
        }

        // 解析当前类字段
        for (Field field : clazz.getDeclaredFields()) {
            ProtoField pf = parseField(field, fieldPOs, isCommon);
            pfs.add(pf);
        }

        po.setProtoFields(pfs);
        po.setProtoFieldObjects(fieldPOs);
        return po;
    }

    /**
     * 解析枚举类
     */
    public ProtoObject doParseEnum(Class<?> clazz) {
        ProtoObject pe = new ProtoObject(clazz);

        List<ProtoField> pfs = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            // 枚举的字段无需解析
            pfs.add(new ProtoField(field));
        }
        pe.setProtoFields(pfs);
        return pe;
    }

    /**
     * 解析类字段
     */
    private ProtoField parseField(Field field, List<ProtoObject> fieldPOs, boolean isCommon) {
        ProtoField protoField = new ProtoField(field);

        // 解析字段类型，或其范型类型
        if (protoField.isCollection() || protoField.isMap()) {
            try {
                // 解析范型（第一次解析不缓存）
//                System.out.println(field.getDeclaringClass() + " ====== " + field.getType() + " ====== " + field.getName());
                ProtoFieldGeneric protoFieldGeneric = doParseFieldGeneric(field, field.getGenericType(), fieldPOs, isCommon);
                protoField.setGeneric(protoFieldGeneric);
            } catch (Exception e) {
                throw new RuntimeException("Collection must use generic definition like List<String>, please check field name " +
                        field.getName() + " at class " + field.getDeclaringClass().getName());
            }
        } else {
            Class<?> fieldType = field.getType();
            protoField.setClazz(fieldType);
            // 解析字段类型
            ProtoFieldType pfType = ProtoUtil.getProtoFieldType(fieldType);
            protoField.setProtoFieldType(pfType);

            // 解析非基础类型字段
            if (pfType == ProtoFieldType.OBJECT || pfType == ProtoFieldType.ENUM) {
                // 如果字段类型就是所属类的类型，则跳过解析
                if (pfType == ProtoFieldType.OBJECT && fieldType.equals(field.getDeclaringClass())) {
                    return protoField;
                }
                ProtoObject fieldPO = parseObjectOrEnum(fieldType, isCommon, pfType);
                fieldPOs.add(fieldPO);
            }
        }
        return protoField;
    }

    /**
     * 解析范型
     */
    private ProtoFieldGeneric parseFieldGeneric(Field field, Type type, List<ProtoObject> fieldPOs, boolean isCommon)
            throws Exception {
        Map<Type, ProtoFieldGeneric> cache = registry.getFieldGenerics();
        if (cache.containsKey(type)) {
            return cache.get(type);
        }

        ProtoFieldGeneric pfg = doParseFieldGeneric(field, type, fieldPOs, isCommon);
        cache.put(type, pfg);
        return pfg;
    }

    /**
     * 解析范型
     */
    private ProtoFieldGeneric doParseFieldGeneric(Field field, Type type, List<ProtoObject> fieldPOs, boolean isCommon)
            throws Exception {
        // No:  List list = ...  or  Map map = ...
        if (!(type instanceof ParameterizedType)) {
            throw new Exception();
        }

        ProtoFieldGeneric generic = new ProtoFieldGeneric((ParameterizedType) type);
        Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
        for (Type paramType : paramTypes) {
            // No:  List<?> list = ...  or  Map<?, ?> map = ...
            if (paramType instanceof WildcardType) {
                throw new Exception();
            }

            // 解析类
            if (paramType instanceof Class<?>) {
                Class<?> paramClass = (Class<?>) paramType;
                generic.addClass(paramClass);
                ProtoFieldType pfType = ProtoUtil.getProtoFieldType(paramClass);
                generic.addProtoFieldType(pfType);

                if (pfType == ProtoFieldType.ENUM || pfType == ProtoFieldType.OBJECT) {
                    // 如果范型类型就是所属类的类型，则跳过解析
                    if (pfType == ProtoFieldType.OBJECT && paramClass.equals(field.getDeclaringClass())) {
                        return generic;
                    }
                    ProtoObject po = parseObjectOrEnum(paramClass, isCommon, pfType);
                    if (fieldPOs != null) {
                        fieldPOs.add(po);
                    }
                }
            } else {
                // 解析嵌套范型
                ProtoFieldGeneric protoFieldGeneric = parseFieldGeneric(field, paramType, fieldPOs, isCommon);
                generic.setGeneric(protoFieldGeneric);
            }
        }
        return generic;
    }
}
