package com.dld.hll.protobuf.generator.service.vo;


import com.dld.hll.protobuf.generator.annotation.Comment;

import java.util.List;

/**
 * @author Chen Hui
 */
public class CompanyVO {
    @Comment("公司名称")
    private String name;

    @Comment("公司规模")
    private int scale;

    @Comment("子公司")
    private CompanyVO sub;

    @Comment("子公司")
    private List<CompanyVO> subs;

    @Comment("子公司")
    private List<List<CompanyVO>> companyList;
}
