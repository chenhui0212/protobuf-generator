package com.dld.hll.protobuf.generator.selector;

/**
 * @author Chen Hui
 */
public interface ServiceSelector {

    boolean accept(Class<?> serviceInterface);
}
