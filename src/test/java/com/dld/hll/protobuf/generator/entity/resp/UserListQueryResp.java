package com.dld.hll.protobuf.generator.entity.resp;

import com.dld.hll.protobuf.generator.service.vo.UserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserListQueryResp extends BaseResp {
    private List<UserVO> user;
}
