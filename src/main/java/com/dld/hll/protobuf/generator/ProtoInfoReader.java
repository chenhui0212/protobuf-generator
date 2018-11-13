package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.util.AssertUtils;
import lombok.Setter;

import java.util.List;

/**
 * @author Chen Hui
 */
@Setter
public class ProtoInfoReader {

    private ProtoInfoRegistry registry;


    public void load(List<Class<?>> serviceClasses) {
        AssertUtils.isTrue(serviceClasses != null && serviceClasses.size() > 0,
                "don't found any service interface");
        ProtoInfoParser protoInfoParser = new ProtoInfoParser(registry);
        for (Class<?> clazz : serviceClasses) {
            protoInfoParser.parseService(clazz);
        }
    }
}
