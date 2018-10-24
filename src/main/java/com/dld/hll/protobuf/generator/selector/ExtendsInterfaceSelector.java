package com.dld.hll.protobuf.generator.selector;

import lombok.AllArgsConstructor;

/**
 * @author Chen Hui
 */
@AllArgsConstructor
public class ExtendsInterfaceSelector implements ServiceSelector {

    private Class<?> extendsInterface;

    @Override
    public boolean accept(Class<?> serviceInterface) {
        Class<?>[] interfaces = serviceInterface.getInterfaces();
        if (interfaces.length == 0) {
            return false;
        }

        for (Class<?> inf : interfaces) {
            if (inf == extendsInterface) {
                return true;
            }
        }

        for (Class<?> inf : interfaces) {
            if (accept(inf)) {
                return true;
            }
        }

        return false;
    }
}
