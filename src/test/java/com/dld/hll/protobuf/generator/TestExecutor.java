package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.annotation.Comment;

public class TestExecutor {
    public static void main(String[] args) throws Exception {
        ProtoExecutor.newBuilder()
                .setProjectBasePath("src/test/java")
                .setComment(Comment.class, "value")
                .build().executor();
    }
}
