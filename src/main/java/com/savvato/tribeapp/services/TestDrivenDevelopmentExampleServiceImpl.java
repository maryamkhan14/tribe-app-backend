package com.savvato.tribeapp.services;

import java.util.ArrayList;

public class TestDrivenDevelopmentExampleServiceImpl implements TestDrivenDevelopmentExampleService {
    @Override
    public String[] getString(int length, char ch, int arrayLength) {
        String rtn;
        int index = 1;

        char appendedChar = 'a';

        ArrayList<String> list = new ArrayList<String>();

        for (int arrayIdx = 0; arrayIdx < arrayLength; arrayIdx++) {
            rtn = "";

            for (int i= 0; i < length; i++) {

                do {
                    appendedChar += (char) index;
                } while (appendedChar == ch);

                rtn += appendedChar;
            }

            list.add(rtn);
        }

        String[] result = new String[list.size()];
        return list.toArray(result);
    }
}
