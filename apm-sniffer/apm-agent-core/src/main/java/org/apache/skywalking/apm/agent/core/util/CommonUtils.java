package org.apache.skywalking.apm.agent.core.util;

import java.util.Objects;

/**
 * @author luoyonghua
 * @since 2020-05-09 15:59
 */
public class CommonUtils {

    public static String getPodVar(String name) {
        if (Objects.isNull(name)) {
            return null;
        }

        String property = System.getProperty(name);
        if (StringUtils.isEmpty(property)) {
            return System.getenv(name);
        }
        return property;
    }
}
