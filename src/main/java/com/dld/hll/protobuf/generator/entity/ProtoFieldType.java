package com.dld.hll.protobuf.generator.entity;

/**
 * @author Chen Hui
 */
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
    BIG_DECIMAL("google.protobuf.StringValue"),
    LOCAL_DATE("google.protobuf.StringValue"),
    LOCAL_DATETIME("google.protobuf.StringValue"),
    LOCAL_TIME("google.protobuf.StringValue"),
    TIMESTAMP("google.protobuf.StringValue"),
    ENUM("enum"),
    OBJECT("message");

    private String name;

    ProtoFieldType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}