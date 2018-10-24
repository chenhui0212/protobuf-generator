package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.*;
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

    public void assignCommon(Class<? extends Annotation> common, String method) throws NoSuchMethodException {
        AbstractProtoInfo.common = common;
        AbstractProtoInfo.method = common.getMethod(method);
    }

    public void generate(File generatePath) {
        // 生成共同类
        writeToFile(generatePath, "Common.proto", generateCommon());

        // 生成全部的接口类
        for (ProtoService protoService : registry.getProtoServices()) {
            writeToFile(generatePath, protoService.getName() + ".proto",
                    generateService(protoService));
        }
    }

    private String generateService(ProtoService protoService) {
        StringBuilder buf = new StringBuilder();
        buf.append(generateHeader(protoService));

        // 获取要生成的接口相关类
        Collection<ProtoObject> protoObjects = protoService.getProtoObjectMap().values();
        buf.append(generateObjects(protoObjects));

        // 生成服务接口描述
        if (protoService.hasDescription()) {
            buf.append(protoService.getDescription()).append("\n");
        }
        buf.append("service ").append(protoService.getClazz().getSimpleName()).append(" {").append("\n");
        for (ProtoMethod protoMethod : protoService.getProtoMethods()) {
            buf.append(generateMethod(protoMethod));
        }
        buf.append("}\n");
        return buf.toString();
    }

    private String generateCommon() {
        StringBuilder buf = new StringBuilder();
        buf.append(generateCommonHeader());

        for (ProtoObject protoObject : registry.getCommonPOs().values()) {
            buf.append(generateObject(protoObject)).append("\n");
        }
        for (ProtoFieldGeneric fieldGeneric : registry.getFieldGenerics().values()) {
            buf.append(generateFieldGeneric(fieldGeneric)).append("\n");
        }

        buf.append(generateEmptyMessage()).append("\n");
        return buf.toString();
    }

    private String generateEmptyMessage() {
        return "message Empty {\n}";
    }

    public StringBuilder generateObjects(Collection<ProtoObject> protoObjects) {
        StringBuilder buf = new StringBuilder();
        for (ProtoObject protoObject : protoObjects) {
            if (Enum.class.isAssignableFrom(protoObject.getClazz())) {
                buf.append(generateEnum(protoObject)).append("\n");
            } else {
                buf.append(generateObject(protoObject)).append("\n");
            }
        }
        return buf;
    }

    public StringBuilder generateObject(ProtoObject protoObject) {
        StringBuilder buf = new StringBuilder();
        if (protoObject.hasDescription()) {
            buf.append(protoObject.getDescription()).append("\n");
        }
        buf.append("message ").append(protoObject.getClazz().getSimpleName()).append(" {\n");
        int index = 1;
        for (ProtoField protoField : protoObject.getProtoFields()) {
            buf.append(generateField(protoField, index++));
        }
        buf.append("}\n");
        return buf;
    }

    public StringBuilder generateEnum(ProtoObject protoEnum) {
        StringBuilder buf = new StringBuilder();
        if (protoEnum.hasDescription()) {
            buf.append(protoEnum.getDescription()).append("\n");
        }
        buf.append("enum ").append(protoEnum.getClazz().getSimpleName()).append(" {\n");
        buf.append("    UNSPECIFIED = 0; // always use a zero value here\n");
        int index = 1;
        for (ProtoField protoField : protoEnum.getProtoFields()) {
            buf.append(generateEnumField(protoField, index++));
        }
        buf.append("}\n");
        return buf;
    }

    public StringBuilder generateMethod(ProtoMethod protoMethod) {
        StringBuilder buf = new StringBuilder();
        if (protoMethod.hasDescription()) {
            buf.append("    ").append(protoMethod.getDescription()).append("\n");
        }
        buf.append("    ").append("rpc ").append(protoMethod.getName()).append(" (")
                .append(protoMethod.getParameterTypeName()).append(")")
                .append(" returns (").append(protoMethod.getReturnTypeName()).append(");\n");
        return buf;
    }

    public StringBuilder generateField(ProtoField protoField, int index) {
        StringBuilder buf = new StringBuilder();
        if (protoField.hasDescription()) {
            buf.append("    ").append(protoField.getDescription()).append("\n");
        }
        buf.append("    ");
        if (protoField.isCollection() || protoField.isMap()) {
            buf.append(protoField.getGeneric().getProtoType());
        } else {
            buf.append(protoField.getTypeName());
        }
        buf.append(" ").append(protoField.getName());
        buf.append(" = ").append(index).append(";\n");
        return buf;
    }

    public StringBuilder generateEnumField(ProtoField protoField, int index) {
        StringBuilder buf = new StringBuilder();
        if (protoField.hasDescription()) {
            buf.append("    ").append(protoField.getDescription()).append("\n");
        }
        buf.append("    ");
        buf.append(protoField.getName());
        buf.append(" = ").append(index).append(";\n");
        return buf;
    }

    private StringBuilder generateFieldGeneric(ProtoFieldGeneric protoFieldGeneric) {
        StringBuilder buf = new StringBuilder();
        String typeName = protoFieldGeneric.getTypeName();
        buf.append("message ").append(typeName).append(" {\n");
        buf.append("    ");
        buf.append(protoFieldGeneric.getProtoType());
        char[] fieldName = typeName.toCharArray();
        fieldName[0] += 32;
        buf.append(" ").append(fieldName);
        buf.append(" = ").append(1).append(";\n");
        buf.append("}\n");
        return buf;
    }

    private StringBuilder generateHeader(ProtoService protoService) {
        StringBuilder buf = new StringBuilder();
        buf.append("syntax = \"proto3\"").append(";\n").append("\n");
        buf.append("option java_multiple_files = true;\n");
        String packagePath = protoService.getClazz().getPackage().getName();
        buf.append("option java_package = \"").append(packagePath).append(".grpc\";\n");
        buf.append("option java_outer_classname = \"").append(protoService.getClazz().getSimpleName())
                .append("Class").append("\";\n");
        buf.append("import \"Common.proto\";\n");
        buf.append("import \"google/protobuf/wrappers.proto\";\n\n");
        return buf;
    }

    private StringBuilder generateCommonHeader() {
        StringBuilder buf = new StringBuilder();
        buf.append("syntax = \"proto3\"").append(";\n").append("\n");
        buf.append("option java_multiple_files = true;\n");
        buf.append("option java_package = \"").append(getCommonPackagePath()).append(".grpc\";\n");
        buf.append("option java_outer_classname = \"").append("Common").append("Class").append("\";\n");
        buf.append("import \"google/protobuf/wrappers.proto\";\n\n");
        return buf;
    }

    private String getCommonPackagePath() {
        List<ProtoService> protoServices = registry.getProtoServices();
        AssertUtil.notEmpty(protoServices, "not found any service interfaces");
        return protoServices.get(0).getClazz().getPackage().getName();
    }

    private void writeToFile(File path, String fileName, String content) {
        if (!path.exists()) {
            boolean result = path.mkdirs();
            if (!result) {
                System.out.println("无法创建基础目录：" + path.getAbsolutePath());
            }
        }

        File file = new File(path, fileName);
        if (file.exists()) {
            boolean result = file.delete();
            if (!result) {
                System.out.println("文件无法被删除：" + file.getAbsolutePath());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
