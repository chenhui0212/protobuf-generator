package com.dld.hll.protobuf.generator.service.vo;

public enum SexEnum {
    MALE(1),
    FEMALE(2);

    private final int value;

    SexEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}