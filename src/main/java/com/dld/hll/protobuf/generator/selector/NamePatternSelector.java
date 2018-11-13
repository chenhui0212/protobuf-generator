package com.dld.hll.protobuf.generator.selector;

import lombok.AllArgsConstructor;

import java.util.regex.Pattern;

/**
 * @author Chen Hui
 */
@AllArgsConstructor
public class NamePatternSelector implements ServiceSelector {

    private String pattern;


    @Override
    public boolean accept(Class<?> serviceInterface) {
        return Pattern.matches(pattern, serviceInterface.getSimpleName());
    }
}
