package org.apache.skywalking.apm.agent.core.util;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luoyonghua
 * @since 2020-04-26 13:44
 */
public class ThreadLocalLabel {

    private final static TransmittableThreadLocal<Map<String, String>> envLabel = new TransmittableThreadLocal<>();

    public static String get(String key) {
        init0();
        return envLabel.get().get(key);
    }

    public static void set(String key, String value) {
        init0();
        envLabel.get().put(key, value);
    }

    public static void removeLabel() {
        envLabel.remove();
    }

    private ThreadLocalLabel() {
    }

    private static void init0() {
        Map<String, String> holder = envLabel.get();
        if (null == holder) {
            envLabel.set(new HashMap<>(8));
        }
    }
}
