package com.dld.hll.protobuf.generator.scanner;

import java.io.IOException;
import java.util.List;

/**
 * 扫描器
 *
 * @author Chen Hui
 */
public interface ProtoInfoScanner {
    List<Class<?>> scanServices() throws IOException, ClassNotFoundException;

    List<Class<?>> scanServices(String scanPackage) throws IOException, ClassNotFoundException;
}
