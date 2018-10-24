package com.dld.hll.protobuf.generator.entity.resp;

import com.dld.hll.protobuf.generator.annotation.Comment;
import lombok.Data;

@Data
public class BaseResp {
    @Comment("返回码")
    private String resCode;

    @Comment("返回信息")
    private String resMsg;
}