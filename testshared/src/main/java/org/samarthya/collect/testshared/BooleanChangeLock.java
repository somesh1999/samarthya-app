package org.samarthya.collect.testshared;

import org.samarthya.collect.shared.locks.ChangeLock;

import java.util.function.Function;

public class BooleanChangeLock implements ChangeLock {

    private boolean locked;

    @Override
    public <T> T withLock(Function<Boolean, T> function) {
        return function.apply(!locked);
    }

    public void lock() {
        locked = true;
    }
}
