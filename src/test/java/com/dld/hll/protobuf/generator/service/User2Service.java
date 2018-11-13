package com.dld.hll.protobuf.generator.service;

import com.dld.hll.protobuf.generator.annotation.Comment;
import com.dld.hll.protobuf.generator.entity.req.UserQueryReq;
import com.dld.hll.protobuf.generator.entity.resp.UserQuery2Resp;

@Comment(value = "用户基础服务2")
public interface User2Service {

    @Comment(value = "查询用户信息")
    UserQuery2Resp query(UserQueryReq request);
}
