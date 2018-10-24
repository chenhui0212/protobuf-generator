package com.dld.hll.protobuf.generator.entity.req;

import com.dld.hll.protobuf.generator.annotation.Comment;
import lombok.Data;

@Data
public class BaseReq {
    @Comment("请求头")
    private RequestHeader header = new RequestHeader();
}