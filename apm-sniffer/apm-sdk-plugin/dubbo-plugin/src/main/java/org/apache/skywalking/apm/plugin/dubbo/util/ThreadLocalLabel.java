package org.apache.skywalking.apm.plugin.dubbo.util;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author luoyonghua
 * @since 2020-04-26 11:38
 */
public class ThreadLocalLabel {
    private static TransmittableThreadLocal<String> envLabel = new TransmittableThreadLocal<>();

    public static String get() {
        return envLabel.get();
    }

    public static void set(String id) {
        envLabel.set(id);
    }

    private ThreadLocalLabel() {
    }
}
