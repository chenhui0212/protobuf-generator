package com.dld.hll.protobuf.generator.service.vo;

import com.dld.hll.protobuf.generator.annotation.Comment;
import lombok.Data;

import java.util.List;

/**
 * @author Chen Hui
 */
@Data
public class UserVO {
    @Comment(value = "用户ID")
    private Long id;

    @Comment(value = "用户名")
    private String name;

    @Comment(value = "性别")
    private SexEnum sex;

    @Comment(value = "公司列表")
    private List<CompanyVO> companyList;
}
