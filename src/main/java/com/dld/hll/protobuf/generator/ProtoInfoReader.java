package com.dld.hll.protobuf.generator;

import com.dld.hll.protobuf.generator.entity.ProtoMethod;
import com.dld.hll.protobuf.generator.entity.ProtoObject;
import com.dld.hll.protobuf.generator.entity.ProtoService;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @author Chen Hui
 */
@Setter
public class ProtoInfoReader {

    private ProtoInfoRegistry registry;

    public void load(List<Class<?>> serviceClasses) {
        AssertUtil.isTrue(serviceClasses != null && serviceClasses.size() > 0,
                "don't found any service interface");
        ProtoInfoParser protoInfoParser = new ProtoInfoParser(registry);
        for (Class<?> clazz : serviceClasses) {
            protoInfoParser.parseService(clazz);
        }
        pullUpCommon();
    }

    /**
     *
     */
    private void pullUpCommon() {
        for (ProtoService protoService : registry.getProtoServices()) {
            extractAllProtoObjects(protoService);
        }

        Collection<Class<?>> common = new HashSet<>();
        for (ProtoService psi : registry.getProtoServices()) {
            for (ProtoService psj : registry.getProtoServices()) {
                if (psj.getClazz() != psi.getClazz()) {
                    common.addAll(CollectionUtils.intersection(psi.getProtoObjectMap().keySet(), psj.getProtoObjectMap().keySet()));
                }
            }
        }

        if (common.size() > 0) {
            for (Class<?> clazz : common) {
                registry.getCommonPOs().put(clazz, registry.getServicePOs().get(clazz));
                registry.getServicePOs().remove(clazz);
            }

            for (ProtoService protoService : registry.getProtoServices()) {
                Map<Class<?>, ProtoObject> protoObjectMap = protoService.getProtoObjectMap();
                for (Class<?> clazz : CollectionUtils.intersection(protoObjectMap.keySet(), common)) {
                    protoObjectMap.remove(clazz);
                }
            }
        }
    }

    /**
     * 初始化每个服务接口要生成的类
     */
    private void extractAllProtoObjects(ProtoService protoService) {
        Map<Class<?>, ProtoObject> protoObjectMap = new HashMap<>();
        for (ProtoMethod protoMethod : protoService.getProtoMethods()) {
            if (protoMethod.getParameterProtoObject() != null) {
                getServiceProtoObjects(protoMethod.getParameterProtoObject(), protoObjectMap);
            }
            if (protoMethod.getReturnProtoObject() != null) {
                getServiceProtoObjects(protoMethod.getReturnProtoObject(), protoObjectMap);
            }
        }
        protoService.setProtoObjectMap(protoObjectMap);
    }

    /**
     * 递归添加类及其类字段
     */
    private void getServiceProtoObjects(ProtoObject protoObject, Map<Class<?>, ProtoObject> protoObjects) {
        // 排除 Common 中已经存在的
        if (!registry.getCommonPOs().containsKey(protoObject.getClazz())) {
            protoObjects.putIfAbsent(protoObject.getClazz(), protoObject);
        }

        if (protoObject.getProtoFieldObjects() != null) {
            for (ProtoObject protoFieldObject : protoObject.getProtoFieldObjects()) {
                getServiceProtoObjects(protoFieldObject, protoObjects);
            }
        }
    }
}
