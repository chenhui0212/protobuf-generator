package com.dld.hll.protobuf.generator.entity.req;

import com.dld.hll.protobuf.generator.annotation.Comment;
import com.dld.hll.protobuf.generator.service.vo.SexEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAddReq extends UserBaseReq {

    @Comment(value = "用户名称")
    private String name;

    @Comment(value = "密码")
    private String password;

    @Comment(value = "性别")
    private SexEnum sex;

    @Comment(value = "生日")
    private Timestamp birthday;
}
