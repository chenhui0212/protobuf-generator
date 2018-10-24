package com.dld.hll.protobuf.generator.service;

import com.dld.hll.protobuf.generator.annotation.Comment;
import com.dld.hll.protobuf.generator.entity.req.BaseReq;
import com.dld.hll.protobuf.generator.entity.req.GenericReq;
import com.dld.hll.protobuf.generator.entity.req.UserAddReq;
import com.dld.hll.protobuf.generator.entity.req.UserQueryReq;
import com.dld.hll.protobuf.generator.entity.resp.BaseResp;
import com.dld.hll.protobuf.generator.entity.resp.GenericResp;
import com.dld.hll.protobuf.generator.entity.resp.UserListQueryResp;
import com.dld.hll.protobuf.generator.entity.resp.UserQueryResp;

@Comment(value = "用户基础服务")
public interface UserService {

    @Comment(value = "查询用户信息")
    BaseResp add(UserAddReq request);

    @Comment(value = "查询用户信息")
    UserQueryResp query(UserQueryReq request);

    @Comment(value = "查询全部用户信息")
    UserListQueryResp queryAll(BaseReq request);

    @Comment(value = "测试空请求空返回值")
    void test();

    @Comment(value = "测试List嵌套")
    GenericResp testList(GenericReq request);
}