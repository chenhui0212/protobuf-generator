package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.*;
import com.dld.hll.protobuf.generator.util.AssertUtils;
import com.dld.hll.protobuf.generator.util.ProtoUtils;
import com.dld.hll.protobuf.generator.util.StringUtils;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Hui
 */
@Setter
public class ProtoFileGenerator {

    private ProtoInfoRegistry registry;

    private static final String SPACES_FOR_INDENTATION = "    ";
    private String commonProtoFileName;


    /**
     * 指定全局注释类型和获取方法
     */
    public void assignCommon(Class<? extends Annotation> commentType, String valueMethod) {
        ProtoCommentSupport.commentType = commentType;
        try {
            ProtoCommentSupport.valueMethod = commentType.getMethod(valueMethod);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成proto文件
     */
    public void generate(File generatePath) {
        // 如果路径不存在，则创建目录
        if (!generatePath.exists()) {
            boolean result = generatePath.mkdirs();
            if (!result) {
                throw new RuntimeException("Could not make directory -> " + generatePath.getAbsolutePath());
            }
        }

        // 如果存在，则删除目录下全部proto文件
        else {
            File[] protoFiles = generatePath.listFiles((file, s) -> s.endsWith(".proto"));
            if (protoFiles != null) {
                for (File pf : protoFiles) {
                    boolean result = pf.delete();
                    if (!result) {
                        throw new RuntimeException("File could not be deleted -> " + pf.getAbsolutePath());
                    }
                }
            }
        }

        // 生成共同类
        writeToFile(generatePath, commonProtoFileName + ".proto", generateCommon());

        // 生成全部的接口类
        for (ProtoService protoService : registry.getProtoServices()) {
            writeToFile(generatePath, protoService.getName() + ".proto",
                    generateService(protoService));
        }
    }

    /**
     * 生成Common proto文件
     */
    private String generateCommon() {
        StringBuilder buf = new StringBuilder();
        buf.append(generateCommonHeader());

        // 共同的类
        for (ProtoObject protoObject : registry.getProtoObjectMap().values()) {
            if (protoObject.getCitations() > 1) {
                if (Enum.class.isAssignableFrom(protoObject.getClazz())) {
                    buf.append(generateEnum(protoObject)).append("\n");
                } else {
                    buf.append(generateObject(protoObject)).append("\n");
                }
            }
        }

        // 共同泛型
        for (ProtoGenericField genericField : registry.getGenericFieldMap().values()) {
            if (genericField.getCitations() > 1) {
                buf.append(generateGenericFieldObject(genericField)).append("\n");
            }
        }

        // 空消息
        buf.append(generateEmptyMessage()).append("\n");
        return buf.toString();
    }

    /**
     * 生成单个服务proto文件
     */
    private String generateService(ProtoService protoService) {
        StringBuilder buf = new StringBuilder();
        buf.append(generateHeader(protoService));

        // 获取要生成的接口相关类
        Collection<ProtoObject> protoObjects = protoService.getProtoObjectMap().values();
        buf.append(generateObjects(protoObjects));

        // 生成泛型类型
        for (ProtoGenericField genericField : protoService.getGenericFieldMap().values()) {
            if (genericField.getCitations() == 1) {
                buf.append(generateGenericFieldObject(genericField)).append("\n");
            }
        }

        // 生成服务接口描述
        generateComment(buf, protoService);
        buf.append("service ").append(protoService.getClazz().getSimpleName()).append(" {").append("\n");
        for (ProtoMethod protoMethod : protoService.getProtoMethods()) {
            buf.append(generateMethod(protoMethod));
        }
        buf.append("}\n");
        return buf.toString();
    }

    /**
     * 生成空消息（备用）
     */
    private String generateEmptyMessage() {
        return "message Empty {\n}";
    }

    /**
     * 生成每个服务的全部proto对象
     */
    private StringBuilder generateObjects(Collection<ProtoObject> protoObjects) {
        StringBuilder buf = new StringBuilder();
        for (ProtoObject protoObject : protoObjects) {
            if (protoObject.getCitations() == 1) {
                if (Enum.class.isAssignableFrom(protoObject.getClazz())) {
                    buf.append(generateEnum(protoObject)).append("\n");
                } else {
                    buf.append(generateObject(protoObject)).append("\n");
                }
            }
        }
        return buf;
    }

    /**
     * 生成对象类型
     */
    private StringBuilder generateObject(ProtoObject protoObject) {
        StringBuilder buf = new StringBuilder();
        generateComment(buf, protoObject);
        buf.append("message ").append(protoObject.getClazz().getSimpleName()).append(" {\n");
        int index = 1;
        index = generateSuperObject(protoObject, buf, index);
        for (ProtoField protoField : protoObject.getProtoFields()) {
            buf.append(generateField(protoField, index++));
        }
        buf.append("}\n");
        return buf;
    }

    /**
     * 生成对象父类字段
     */
    private int generateSuperObject(ProtoObject protoObject, StringBuilder buf, int index) {
        ProtoObject superProtoObject = protoObject.getSuperProtoObject();
        if (superProtoObject != null) {
            // 优先生成上层父类
            index = generateSuperObject(superProtoObject, buf, index);
            for (ProtoField protoField : superProtoObject.getProtoFields()) {
                buf.append(generateField(protoField, index++));
            }
        }
        return index;
    }

    /**
     * 生成枚举类型
     */
    private StringBuilder generateEnum(ProtoObject protoEnum) {
        StringBuilder buf = new StringBuilder();
        generateComment(buf, protoEnum);
        buf.append("enum ").append(protoEnum.getClazz().getSimpleName()).append(" {\n");
        buf.append("    UNSPECIFIED = 0; // always use a zero value here\n");
        int index = 1;
        for (ProtoField protoField : protoEnum.getProtoFields()) {
            buf.append(generateEnumField(protoField, index++));
        }
        buf.append("}\n");
        return buf;
    }

    /**
     * 生成方法
     */
    private StringBuilder generateMethod(ProtoMethod protoMethod) {
        StringBuilder buf = new StringBuilder();
        generateComment(buf, protoMethod, SPACES_FOR_INDENTATION);
        buf.append(SPACES_FOR_INDENTATION).append("rpc ").append(protoMethod.getName()).append(" (")
                .append(protoMethod.getParameterTypeName()).append(")")
                .append(" returns (").append(protoMethod.getReturnTypeName()).append(");\n");
        return buf;
    }

    /**
     * 生成字段
     */
    private StringBuilder generateField(ProtoField protoField, int index) {
        StringBuilder buf = new StringBuilder();
        generateComment(buf, protoField, SPACES_FOR_INDENTATION);
        buf.append(SPACES_FOR_INDENTATION);
        if (ProtoUtils.isNotGeneric(protoField.getFieldType())) {
            buf.append(protoField.getTypeName());
        } else {
            buf.append(generateGenericField(protoField.getGeneric()));
        }
        buf.append(" ").append(protoField.getName());
        buf.append(" = ").append(index).append(";\n");
        return buf;
    }

    /**
     * 生成枚举字段
     */
    private StringBuilder generateEnumField(ProtoField protoField, int index) {
        StringBuilder buf = new StringBuilder();
        generateComment(buf, protoField, SPACES_FOR_INDENTATION);
        buf.append(SPACES_FOR_INDENTATION);
        buf.append(protoField.getName());
        buf.append(" = ").append(index).append(";\n");
        return buf;
    }

    /**
     * 生成泛型内嵌对象
     */
    private StringBuilder generateGenericFieldObject(ProtoGenericField genericField) {
        StringBuilder buf = new StringBuilder();
        String typeName = genericField.getTypeName();
        buf.append("message ").append(typeName).append(" {\n");
        buf.append(SPACES_FOR_INDENTATION);
        buf.append(generateGenericField(genericField));
        buf.append(" ").append(StringUtils.uncapitalize(typeName));
        buf.append(" = ").append(1).append(";\n");
        buf.append("}\n");
        return buf;
    }

    /**
     * 生成泛型字段
     */
    private StringBuilder generateGenericField(ProtoGenericField genericField) {
        StringBuilder buf = new StringBuilder();
        if (ProtoUtils.isCollection(genericField.getType().getRawType())) {
            buf.append("repeated ");
            if (genericField.isNotGeneric()) {
                buf.append(ProtoUtils.getTypeName(genericField.getParameterTypes().get(0), genericField.getProtoFieldTypes().get(0)));
            } else {
                buf.append(genericField.getNestedGeneric().getTypeName());
            }
        } else if (ProtoUtils.isMap(genericField.getType().getRawType())) {
            AssertUtils.isTrue(genericField.getProtoFieldTypes() != null && genericField.getParameterTypes().size() > 0,
                    "nestedGeneric field parse not right");
            buf.append("map<");
            if (genericField.isNotGeneric()) {
                buf.append(ProtoUtils.getTypeName(genericField.getParameterTypes().get(0), genericField.getProtoFieldTypes().get(0)));
                buf.append(", ");
                buf.append(ProtoUtils.getTypeName(genericField.getParameterTypes().get(1), genericField.getProtoFieldTypes().get(1)));
            } else {
                buf.append(ProtoUtils.getTypeName(genericField.getParameterTypes().get(0), genericField.getProtoFieldTypes().get(0)));
                buf.append(", ");
                buf.append(genericField.getNestedGeneric().getTypeName());
            }
            buf.append(">");
        }
        return buf;
    }

    /**
     * 生成服务头
     */
    private StringBuilder generateHeader(ProtoService protoService) {
        StringBuilder buf = new StringBuilder();
        buf.append("syntax = \"proto3\"").append(";\n").append("\n");
        buf.append("option java_multiple_files = true;\n");
        String packagePath = protoService.getClazz().getPackage().getName();
        buf.append("option java_package = \"").append(packagePath).append(".grpc\";\n");
        buf.append("option java_outer_classname = \"").append(protoService.getClazz().getSimpleName())
                .append("Class").append("\";\n");
        buf.append("import \"").append(commonProtoFileName).append(".proto\";\n\n");
        buf.append("import \"google/protobuf/wrappers.proto\";\n\n");
        return buf;
    }

    /**
     * 生成Common头
     */
    private StringBuilder generateCommonHeader() {
        StringBuilder buf = new StringBuilder();
        buf.append("syntax = \"proto3\"").append(";\n").append("\n");
        buf.append("option java_multiple_files = true;\n");
        buf.append("option java_package = \"").append(getCommonPackagePath()).append(".grpc\";\n");
        buf.append("option java_outer_classname = \"").append(commonProtoFileName).append("Class").append("\";\n\n");
        buf.append("import \"google/protobuf/wrappers.proto\";\n\n");
        return buf;
    }

    /**
     * 生成注释
     */
    private <T extends ProtoCommentSupport> void generateComment(StringBuilder buf, T protoElement) {
        generateComment(buf, protoElement, null);
    }

    /**
     * 生成注释
     */
    private <T extends ProtoCommentSupport> void generateComment(StringBuilder buf, T protoElement, String prefix) {
        String description = protoElement.getComment();
        if (description != null) {
            if (prefix != null) {
                buf.append(prefix);
            }
            buf.append("// ").append(description).append("\n");
        }
    }

    /**
     * 获取Common proto文件生成路径
     */
    private String getCommonPackagePath() {
        List<ProtoService> protoServices = registry.getProtoServices();
        AssertUtils.notEmpty(protoServices, "could not found in any service interfaces");
        return protoServices.get(0).getClazz().getPackage().getName();
    }

    /**
     * 写入文件
     */
    private void writeToFile(File path, String fileName, String content) {
        File file = new File(path, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not write proto file [" + e.getMessage() + "]");
        }
    }
}
