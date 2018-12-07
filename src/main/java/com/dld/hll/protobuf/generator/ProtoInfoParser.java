package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.*;
import com.dld.hll.protobuf.generator.util.ProtoUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chen Hui
 */
public class ProtoInfoParser {

    private ProtoInfoRegistry registry;
    private ProtoService currentService;


    public ProtoInfoParser(ProtoInfoRegistry registry) {
        this.registry = registry;
    }

    /**
     * 解析服务接口类
     */
    public void parseService(Class<?> serviceClass) {
        if (!isContainsMethods(serviceClass)) {
            return;
        }

        currentService = new ProtoService(serviceClass);
        parseCurrentServiceMethods();
        registry.register(currentService);
    }

    private boolean isContainsMethods(Class<?> clazz) {
        return clazz.getDeclaredMethods().length != 0;
    }

    /**
     * 解析接口类中全部方法
     */
    private void parseCurrentServiceMethods() {
        List<ProtoMethod> protoMethods = Arrays.stream(currentService.getServiceClass().getDeclaredMethods())
                .map(this::parseMethod)
                .collect(Collectors.toList());
        currentService.setProtoMethods(protoMethods);
    }

    /**
     * 解析类中方法
     */
    private ProtoMethod parseMethod(Method method) {
        if (method.getParameterTypes().length > 1) {
            throw new RuntimeException("More then one parameter in the method [" + method.getName() + "] of service [" +
                    method.getDeclaringClass().getSimpleName() + "].");
        }

        ProtoMethod protoMethod = new ProtoMethod(method);
        parseMethodParameterType(protoMethod);
        parseMethodReturnType(protoMethod);
        return protoMethod;
    }

    /**
     * 解析方法参数类型
     * 参数个数只可能为零个或一个
     */
    private void parseMethodParameterType(ProtoMethod protoMethod) {
        Class<?>[] parameterTypes = protoMethod.getMethod().getParameterTypes();
        if (parameterTypes.length == 1) {
            protoMethod.setParameterType(parameterTypes[0]);
            ProtoObject protoObject = parseObject(parameterTypes[0]);
            protoMethod.setParameterProtoObject(protoObject);
        }
    }

    /**
     * 解析方法返回值类型
     */
    private void parseMethodReturnType(ProtoMethod protoMethod) {
        Class<?> returnType = protoMethod.getMethod().getReturnType();
        protoMethod.setReturnType(returnType);
        if (returnType != Void.TYPE) {
            ProtoObject protoObject = parseObject(returnType);
            protoMethod.setReturnProtoObject(protoObject);
        }
    }

    private ProtoObject parseObject(Class<?> clazz) {
        return parseObjectOrEnum(clazz, ProtoFieldType.OBJECT);
    }

    private ProtoObject parseObjectOrEnum(Class<?> clazz, ProtoFieldType pfType) {
        ProtoObject protoObject = registry.getFormParsingOrCache(clazz);
        if (protoObject != null) {
            increaseObjectCitations(protoObject);
            return protoObject;
        }

        protoObject = new ProtoObject(clazz);
        registry.addParsing(clazz, protoObject);
        if (pfType == ProtoFieldType.OBJECT) {
            doParseObject(protoObject);
        } else if (pfType == ProtoFieldType.ENUM) {
            doParseEnum(protoObject);
        } else {
            throw new RuntimeException("PFType must be ProtoFieldType.OBJECT or ProtoFieldType.ENUM");
        }
        registry.addCache(clazz, protoObject);
        registry.removeParsing(clazz);

        currentService.addParsed(clazz, protoObject);
        return protoObject;
    }

    /**
     * 解析接口中的类
     */
    private void doParseObject(ProtoObject protoObject) {
        // 解析父类
        Class<?> superClass = protoObject.getClazz().getSuperclass();
        if (superClass != null && superClass != Object.class) {
            ProtoObject superPO = parseObject(superClass);
            protoObject.setSuperProtoObject(superPO);
        }

        // 解析类字段
        List<ProtoField> protoFields = Arrays.stream(protoObject.getClazz().getDeclaredFields())
                .map(this::parseField)
                .collect(Collectors.toList());
        protoObject.setProtoFields(protoFields);
    }

    /**
     * 解析枚举类（无需解析）
     */
    private void doParseEnum(ProtoObject protoObject) {
        List<ProtoField> protoFields = Arrays.stream(protoObject.getClazz().getFields())
                .map(ProtoField::new)
                .collect(Collectors.toList());
        protoObject.setProtoFields(protoFields);
    }

    /**
     * 解析类字段
     */
    private ProtoField parseField(Field field) {
        ProtoField protoField = new ProtoField(field);
        Class<?> fieldType = field.getType();

        // 解析字段类型，或泛型类型
        if (ProtoUtils.isNotGeneric(fieldType)) {
            // 解析字段类型
            ProtoFieldType pfType = ProtoUtils.getProtoFieldType(fieldType);
            protoField.setProtoFieldType(pfType);

            // 解析非基础类型字段
            if (pfType == ProtoFieldType.OBJECT || pfType == ProtoFieldType.ENUM) {
                ProtoObject fieldPO = parseObjectOrEnum(fieldType, pfType);
                protoField.setProtoObject(fieldPO);
            }
        } else {
            ProtoGenericField genericField = parseGenericField(field);
            protoField.setGeneric(genericField);
        }
        return protoField;
    }

    private ProtoGenericField parseGenericField(Field genericField) {
        Type genericType = genericField.getGenericType();
        validateGenericField(genericField, genericType);
        return parseGenericField(genericField, genericType, true);
    }

    /**
     * 解析范型（缓存）
     *
     * @param isOutermost 是否是最外层泛型解析
     */
    private ProtoGenericField parseGenericField(Field field, Type type, boolean isOutermost) {
        if (registry.isParsing(type)) {
            ProtoGenericField genericField = registry.getParsing(type);
            increaseNestedGenericCitations(genericField);
            return genericField;
        }

        ProtoGenericField genericField = registry.getCache(type);
        if (genericField != null) {
            if (isOutermost) {
                increaseOutermostGenericCitations(genericField);
            } else {
                currentService.addParsed(type, genericField);
                increaseNestedGenericCitations(genericField);
            }
            return genericField;
        }

        genericField = new ProtoGenericField(field, (ParameterizedType) type, isOutermost ? 0 : 1);
        registry.addParsing(type, genericField);
        doParseFieldGeneric(genericField);
        registry.addCache(type, genericField);
        registry.removeParsing(type);

        if (!isOutermost) {
            currentService.addParsed(type, genericField);
        }
        return genericField;
    }

    /**
     * 解析范型
     */
    private void doParseFieldGeneric(ProtoGenericField genericField) {
        Type[] paramTypes = genericField.getType().getActualTypeArguments();
        for (Type paramType : paramTypes) {
            // 解析非泛型
            if (paramType instanceof Class) {
                Class<?> paramClass = (Class<?>) paramType;
                genericField.addParameterType(paramClass);
                ProtoFieldType pfType = ProtoUtils.getProtoFieldType(paramClass);
                genericField.addProtoFieldType(pfType);

                // 如果是非一般数据类型，则进一步解析
                if (pfType == ProtoFieldType.ENUM || pfType == ProtoFieldType.OBJECT) {
                    ProtoObject protoObject = parseObjectOrEnum(paramClass, pfType);
                    genericField.addProtoObject(protoObject);
                }
            }
            // 解析嵌套范型
            else {
                ProtoGenericField nestedGenericField = parseGenericField(genericField.getField(), paramType, false);
                genericField.setNestedGeneric(nestedGenericField);
            }
        }
    }

    private void validateGenericField(Field genericField, Type type) {
        // No:  List list = ...;  or  Map map = ...;
        if (!(type instanceof ParameterizedType)) {
            throw new RuntimeException("No parameter type is specified in generic field[" +
                    genericField.getName() + "] of Class[" + genericField.getDeclaringClass().getName() + "].");
        }

        // No:  List<?> list = ...;  or  Map<?, ?> map = ...;
        if (type instanceof WildcardType) {
            throw new RuntimeException("Wildcard is not support for generic field type in field[" +
                    genericField.getName() + "] of Class[" + genericField.getDeclaringClass().getName() + "].");
        }

        Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
        for (Type paramType : paramTypes) {
            if (!(paramType instanceof Class)) {
                validateGenericField(genericField, paramType);
            }
        }
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
     * 增加最外层泛型引用次数
     */
    public void increaseOutermostGenericCitations(ProtoGenericField genericField) {
        // 最外层，且缓存不存在于当前解析Service中，则增加泛型内嵌泛型或者类引用次数
        if (!isExistsInCurrentService(genericField.getType())) {
            for (ProtoObject protoObject : genericField.getProtoObjects()) {
                ProtoUtils.increaseCitations(protoObject);
            }
            if (genericField.getNestedGeneric() != null) {
                ProtoUtils.increaseCitations(genericField.getNestedGeneric());
            }
        }
    }

    /**
     * 增加内嵌泛型的引用次数
     */
    public void increaseNestedGenericCitations(ProtoGenericField genericField) {
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

    public boolean isExistsInCurrentService(Class<?> clazz) {
        return currentService.isExists(clazz) || registry.isParsing(clazz);
    }

    public boolean isExistsInCurrentService(Type type) {
        return currentService.isExists(type) || registry.isParsing(type);
    }
}
