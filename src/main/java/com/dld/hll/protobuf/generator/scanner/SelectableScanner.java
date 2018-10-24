package com.dld.hll.protobuf.generator.scanner;

import com.dld.hll.protobuf.generator.selector.ServiceSelector;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Hui
 */
@Getter
public abstract class SelectableScanner implements ProtoInfoScanner {
    private List<ServiceSelector> selectors;

    public void addSelector(ServiceSelector selector) {
        if (selectors == null) {
            selectors = new ArrayList<>();
        }
        selectors.add(selector);
    }

    boolean isAcceptable(Class<?> clazz) {
        if (selectors == null) {
            return true;
        }

        for (ServiceSelector selector : selectors) {
            if (!selector.accept(clazz)) {
                return false;
            }
        }
        return true;
    }
}
