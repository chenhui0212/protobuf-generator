package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.*;
import com.dld.hll.protobuf.generator.util.ProtoUtils;
import com.dld.hll.protobuf.generator.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Hui
 */
@Setter
@AllArgsConstructor
public class ProtoFileGenerator {

    private ProtoInfoRegistry registry;
    private File generatePath;
    private String commonProtoFileName;

    private static final String INDENTATION_SPACES = "    ";
    private static String lineSeparator = "\n";
    private static String doubleLineSeparator = lineSeparator + lineSeparator;


    /**
     * 生成proto文件
     */
    public void generate() {
        prepareGeneratePath(generatePath);

        writeToFile(commonProtoFileName + ".proto", generateCommon());

        for (ProtoService protoService : registry.getProtoServices()) {
            writeToFile(protoService.getName() + ".proto", generateService(protoService));
        }
    }

    /**
     * 确保干净可用的生成proto文件的目录
     */
    private void prepareGeneratePath(File generatePath) {
        if (!generatePath.exists()) {
            boolean result = generatePath.mkdirs();
            if (!result) {
                throw new RuntimeException("Could not make directory -> " + generatePath.getAbsolutePath());
            }
        } else {
            // 删除目录下全部proto文件
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
    }

    /**
     * 生成Common proto文件
     */
    private String generateCommon() {
        StringBuilder buf = new StringBuilder();
        generateCommonHeader(buf);

        // 共同的类
        for (ProtoObject protoObject : registry.getProtoObjectMap().values()) {
            if (protoObject.getCitations() > 1) {
                if (Enum.class.isAssignableFrom(protoObject.getClazz())) {
                    generateEnum(buf, protoObject);
                } else {
                    generateObject(buf, protoObject);
                }
            }
        }

        // 共同泛型
        for (ProtoGenericField genericField : registry.getGenericFieldMap().values()) {
            if (genericField.getCitations() > 1) {
                generateGenericFieldObject(buf, genericField);
            }
        }

        // 空消息（备用）
        generateEmptyMessage(buf);
        return buf.toString();
    }

    /**
     * 生成单个服务proto文件
     */
    private String generateService(ProtoService protoService) {
        StringBuilder buf = new StringBuilder();
        generateServiceHeader(buf, protoService);

        // 获取要生成的接口相关类
        Collection<ProtoObject> protoObjects = protoService.getProtoObjectMap().values();
        generateObjects(buf, protoObjects);

        // 生成内嵌泛型类型
        for (ProtoGenericField genericField : protoService.getGenericFieldMap().values()) {
            if (genericField.getCitations() == 1) {
                generateGenericFieldObject(buf, genericField);
            }
        }

        // 生成服务接口描述
        generateServiceInterface(buf, protoService);
        return buf.toString();
    }

    /**
     * 生成每个服务的全部proto对象
     */
    private void generateObjects(StringBuilder buf, Collection<ProtoObject> protoObjects) {
        for (ProtoObject protoObject : protoObjects) {
            if (protoObject.getCitations() == 1) {
                if (Enum.class.isAssignableFrom(protoObject.getClazz())) {
                    generateEnum(buf, protoObject);
                } else {
                    generateObject(buf, protoObject);
                }
            }
        }
    }

    /**
     * 生成对象类型
     */
    private void generateObject(StringBuilder buf, ProtoObject protoObject) {
        generateCommentWithIndent(buf, protoObject);
        buf.append("message ").append(protoObject.getClazz().getSimpleName()).append(" {").append(lineSeparator);
        int index = 1;
        index = generateSuperObject(protoObject, buf, index);
        for (ProtoField protoField : protoObject.getProtoFields()) {
            generateField(buf, protoField, index++);
        }
        buf.append("}").append(doubleLineSeparator);
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
                generateField(buf, protoField, index++);
            }
        }
        return index;
    }

    /**
     * 生成枚举类型
     */
    private void generateEnum(StringBuilder buf, ProtoObject protoEnum) {
        generateComment(buf, protoEnum);
        buf.append("enum ").append(protoEnum.getClazz().getSimpleName()).append(" {").append(lineSeparator);
        buf.append(INDENTATION_SPACES).append("UNSPECIFIED = 0;").append(lineSeparator);
        int index = 1;
        for (ProtoField protoField : protoEnum.getProtoFields()) {
            generateEnumField(buf, protoField, index++);
        }
        buf.append("}").append(doubleLineSeparator);
    }

    /**
     * 生成方法
     */
    private void generateMethod(StringBuilder buf, ProtoMethod protoMethod) {
        generateCommentWithIndent(buf, protoMethod);
        buf.append(INDENTATION_SPACES).append("rpc ").append(protoMethod.getName())
                .append(" (").append(protoMethod.getParameterTypeName()).append(") ")
                .append("returns (").append(protoMethod.getReturnTypeName()).append(");").append(lineSeparator);
    }

    /**
     * 生成字段
     */
    private void generateField(StringBuilder buf, ProtoField protoField, int index) {
        generateCommentWithIndent(buf, protoField);
        buf.append(INDENTATION_SPACES);
        if (ProtoUtils.isNotGeneric(protoField.getFieldType())) {
            buf.append(protoField.getTypeName());
        } else {
            generateGenericField(buf, protoField.getGeneric());
        }
        buf.append(" ").append(protoField.getName());
        buf.append(" = ").append(index).append(";").append(lineSeparator);
    }

    /**
     * 生成枚举字段
     */
    private void generateEnumField(StringBuilder buf, ProtoField protoField, int index) {
        generateCommentWithIndent(buf, protoField);
        buf.append(INDENTATION_SPACES).append(protoField.getName());
        buf.append(" = ").append(index).append(";").append(lineSeparator);
    }

    /**
     * 生成泛型内嵌对象
     */
    private void generateGenericFieldObject(StringBuilder buf, ProtoGenericField genericField) {
        String typeName = genericField.getTypeName();
        buf.append("message ").append(typeName).append(" {").append(lineSeparator);
        buf.append(INDENTATION_SPACES);
        generateGenericField(buf, genericField);
        buf.append(" ").append(StringUtils.uncapitalize(typeName));
        buf.append(" = ").append(1).append(";").append(lineSeparator);
        buf.append("}").append(doubleLineSeparator);
    }

    /**
     * 生成泛型字段
     */
    private void generateGenericField(StringBuilder buf, ProtoGenericField genericField) {
        if (ProtoUtils.isCollection(genericField.getType().getRawType())) {
            generateCollectionGenericField(buf, genericField);
        } else if (ProtoUtils.isMap(genericField.getType().getRawType())) {
            generateMapGenericField(buf, genericField);
        }
    }

    /**
     * 生成 Collection 泛型字段
     */
    private void generateCollectionGenericField(StringBuilder buf, ProtoGenericField genericField) {
        buf.append("repeated ");
        if (genericField.isNotGeneric()) {
            buf.append(ProtoUtils.getTypeName(genericField.getParameterTypes().get(0),
                    genericField.getProtoFieldTypes().get(0)));
        } else {
            buf.append(genericField.getNestedGeneric().getTypeName());
        }
    }

    /**
     * 生成 Map 泛型字段
     */
    private void generateMapGenericField(StringBuilder buf, ProtoGenericField genericField) {
        List<Class<?>> parameterTypes = genericField.getParameterTypes();
        List<ProtoFieldType> protoFieldTypes = genericField.getProtoFieldTypes();
        buf.append("map<");
        if (genericField.isNotGeneric()) {
            buf.append(ProtoUtils.getTypeName(parameterTypes.get(0), protoFieldTypes.get(0)));
            buf.append(", ");
            buf.append(ProtoUtils.getTypeName(parameterTypes.get(1), protoFieldTypes.get(1)));
        } else {
            buf.append(ProtoUtils.getTypeName(parameterTypes.get(0), protoFieldTypes.get(0)));
            buf.append(", ");
            buf.append(genericField.getNestedGeneric().getTypeName());
        }
        buf.append(">");
    }

    /**
     * 生成Common头
     */
    private void generateCommonHeader(StringBuilder buf) {
        buf.append("syntax = \"proto3\";").append(doubleLineSeparator);
        buf.append("option java_multiple_files = true;").append(lineSeparator);
        buf.append("option java_package = \"").append(getCommonPackagePath()).append(".grpc\";").append(lineSeparator);
        buf.append("option java_outer_classname = \"").append(commonProtoFileName).append("Class\";")
                .append(doubleLineSeparator);
    }

    /**
     * 生成服务头
     */
    private void generateServiceHeader(StringBuilder buf, ProtoService protoService) {
        buf.append("syntax = \"proto3\";").append(doubleLineSeparator);
        buf.append("option java_multiple_files = true;").append(lineSeparator);
        String packagePath = protoService.getServiceClass().getPackage().getName();
        buf.append("option java_package = \"").append(packagePath).append(".grpc\";").append(lineSeparator);
        buf.append("option java_outer_classname = \"").append(protoService.getServiceClass().getSimpleName())
                .append("Class\";").append(lineSeparator);
        buf.append("import \"").append(commonProtoFileName).append(".proto\";").append(lineSeparator);
        buf.append("import \"google/protobuf/wrappers.proto\";").append(doubleLineSeparator);
    }

    /**
     * 生成空消息
     */
    private void generateEmptyMessage(StringBuilder buf) {
        buf.append("message Empty {").append(lineSeparator).append("}").append(lineSeparator);
    }

    /**
     * 生成服务接口描述
     */
    private void generateServiceInterface(StringBuilder buf, ProtoService protoService) {
        generateComment(buf, protoService);
        buf.append("service ").append(protoService.getServiceClass().getSimpleName()).append(" {").append(lineSeparator);
        for (ProtoMethod protoMethod : protoService.getProtoMethods()) {
            generateMethod(buf, protoMethod);
        }
        buf.append("}").append(lineSeparator);
    }

    /**
     * 生成注释
     */
    private <T extends ProtoCommentSupport> void generateComment(StringBuilder buf, T protoElement) {
        generateCommentWithIndent(buf, protoElement, null);
    }

    /**
     * 生成带缩进的注释
     */
    private <T extends ProtoCommentSupport> void generateCommentWithIndent(StringBuilder buf, T protoElement) {
        generateCommentWithIndent(buf, protoElement, INDENTATION_SPACES);
    }

    /**
     * 生成指定缩进内容的注释
     */
    private <T extends ProtoCommentSupport> void generateCommentWithIndent(
            StringBuilder buf, T protoElement, String prefix) {
        String description = protoElement.getComment();
        if (description != null) {
            if (prefix != null) {
                buf.append(prefix);
            }
            buf.append("// ").append(description).append(lineSeparator);
        }
    }

    private String getCommonPackagePath() {
        ProtoService protoService = registry.getProtoServices().get(0);
        return protoService.getServiceClass().getPackage().getName();
    }

    private void writeToFile(String fileName, String content) {
        File file = new File(generatePath, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not write proto file [" + e.getMessage() + "]");
        }
    }
}
