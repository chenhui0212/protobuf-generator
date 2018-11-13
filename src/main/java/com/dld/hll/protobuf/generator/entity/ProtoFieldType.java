package com.dld.hll.protobuf.generator.entity;

import lombok.AllArgsConstructor;

/**
 * @author Chen Hui
 */
@AllArgsConstructor
public enum ProtoFieldType {
    DOUBLE("double"),
    DOUBLE_VALUE("google.protobuf.DoubleValue"),
    FLOAT("float"),
    FLOAT_VALUE("google.protobuf.FloatValue"),
    LONG("int64"),
    LONG_VALUE("google.protobuf.Int64Value"),
    INT("int32"),
    INT_VALUE("google.protobuf.Int32Value"),
    BOOL("bool"),
    BOOL_VALUE("google.protobuf.BoolValue"),
    STRING("string"),
    STRING_VALUE("google.protobuf.StringValue"),
    BIG_DECIMAL("string"),
    LOCAL_DATE("string"),
    LOCAL_DATETIME("string"),
    LOCAL_TIME("string"),
    TIMESTAMP("string"),
    ENUM("enum"),
    OBJECT("message");

    private String name;

    public String getName() {
        return name;
    }
}