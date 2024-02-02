package com.savvato.tribeapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class})
public class TestDrivenDevelopmentExampleServiceImplTest {
    @TestConfiguration
    static class TestDrivenDevelopmentExampleServiceImplTestContextConfiguration {
        @Bean
        public TestDrivenDevelopmentExampleService testDrivenDevelopmentExampleService() {
            return new TestDrivenDevelopmentExampleServiceImpl();
        }
    }

    @Autowired
    TestDrivenDevelopmentExampleService testDrivenDevelopmentExampleService;

    @Test
    public void getString() {
        Integer strLength = 8;
        char letterToAvoid = 'Z';
        Integer arraySize = 3;
        String[] actualStrings = testDrivenDevelopmentExampleService.getString(strLength, letterToAvoid, arraySize);
        assertEquals(actualStrings.length, arraySize);
        for (String str : actualStrings) {
            assertThat(str).doesNotContain(String.valueOf(letterToAvoid));
            assertEquals(strLength, str.length());
        }
    }
}
