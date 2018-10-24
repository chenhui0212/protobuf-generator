package com.dld.hll.protobuf.generator.entity.req;

import com.dld.hll.protobuf.generator.annotation.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserBaseReq extends BaseReq {
    @Comment(value = "用户ID")
    private Long id;
}
