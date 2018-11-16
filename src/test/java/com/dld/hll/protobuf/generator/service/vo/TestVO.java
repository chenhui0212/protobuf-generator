package com.dld.hll.protobuf.generator.service.vo;

import com.dld.hll.protobuf.generator.annotation.Comment;
import lombok.Data;

/**
 * @author Chen Hui
 */
@Data
public class TestVO {

    @Comment("测试")
    private String test;
}
