package com.savvato.tribeapp.services;

public class TestDrivenDevelopmentExampleServiceImpl implements TestDrivenDevelopmentExampleService {
    @Override
    public String[] getString(int length, char ch, int arrayLength) {
        String rtn = "";
        int index = 1;

        char appendedChar = 'a';

        for (int i= 0; i < length; i++) {

            do {
                appendedChar += (char) index++;
            } while (appendedChar == ch);

            rtn += appendedChar;
        }

        return new String[]{rtn};
    }
}
