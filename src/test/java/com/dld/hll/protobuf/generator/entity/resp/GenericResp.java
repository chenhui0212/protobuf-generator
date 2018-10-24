package com.dld.hll.protobuf.generator.entity.resp;

import java.util.List;
import java.util.Map;

/**
 * @author Chen Hui
 */
public class GenericResp {
    private List<List<String>> listList;

    private List<Map<String, String>> listMap;

    private Map<String, List<String>> mapList;

    private Map<String, Map<String, String>> mapMap;
}
