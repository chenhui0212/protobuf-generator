package com.dld.hll.protobuf.generator.entity.resp;

import com.dld.hll.protobuf.generator.annotation.Comment;
import com.dld.hll.protobuf.generator.service.vo.UserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryResp extends BaseResp {
    @Comment(value = "用户信息")
    private UserVO user;
}
