package com.cloudbees.javanet.cvsnews.util;

import org.junit.Test;

/**
 * @author Oleg Nenashev
 */
public class ConfigTest {

    @Test
    public void shouldLoadConfigByDefault() throws Exception {
        Config cfg = Config.loadConfig();
    }

}
