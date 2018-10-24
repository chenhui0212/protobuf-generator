package com.dld.hll.protobuf.generator.entity.req;

import com.dld.hll.protobuf.generator.annotation.Comment;
import lombok.Data;

@Data
public class RequestHeader {
    @Comment("追踪ID")
    private String trcId;

    @Comment("远程请求IP地址")
    private String remoteIp;
}
