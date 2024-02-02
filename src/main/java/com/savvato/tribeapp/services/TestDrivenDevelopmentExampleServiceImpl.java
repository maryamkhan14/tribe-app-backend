package com.savvato.tribeapp.services;

public class TestDrivenDevelopmentExampleServiceImpl implements TestDrivenDevelopmentExampleService {
    @Override
    public String getString(int length) {
        String rtn = "";

        for (int i= 0; i < length; i++) {
            rtn += "a";
        }

        return rtn;
    }
}
