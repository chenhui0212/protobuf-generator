package com.dld.hll.protobuf.generator.scanner;

import java.util.List;

/**
 * 扫描器
 *
 * @author Chen Hui
 */
public interface ProtoInfoScanner {

    List<Class<?>> scanServices();
}
