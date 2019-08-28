package com.github.udalovsergey.bank.transaction.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the same lock object for different instances of the account.
 * Attention - there is no limit for grow. It can eat a lot of memory.
 * Better way to use Guava Striped here.
 */
public class Lock {

    private Map<Long, Object> locks = new ConcurrentHashMap<>();

    public Object get(Long id) {
        return locks.computeIfAbsent(id, any -> new Object());
    }
}
