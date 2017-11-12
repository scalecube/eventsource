/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustack.jeventstore;

import java.util.function.Function;

/**
 *
 * @author nuwansa
 */
public class BPlusTree {
    Function<String,Long> keyProvider;
    Function<String,Long> valueProvider;

    public BPlusTree(Function<String, Long> keyProvider, Function<String, Long> valueProvider) {
        this.keyProvider = keyProvider;
        this.valueProvider = valueProvider;
    }

    
    public Function<String, Long> getKeyProvider() {
        return keyProvider;
    }

    public Function<String, Long> getValueProvider() {
        return valueProvider;
    }
    
    
}