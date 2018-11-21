package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.annotation.Comment;

public class TestExecutor {
    public static void main(String[] args) {
        ProtoExecutor.newBuilder()
                .setProjectName("protobuf-generator")
                .setProjectBasePath("src/test/java")
                .setGeneratePath("src/test/proto")
                .setComment(Comment.class, "value")
                .build().executor();
    }
}
