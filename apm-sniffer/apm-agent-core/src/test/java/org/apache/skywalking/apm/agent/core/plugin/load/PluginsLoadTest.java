package org.apache.skywalking.apm.agent.core.plugin.load;

import org.apache.skywalking.apm.agent.core.plugin.PluginBootstrap;
import org.apache.skywalking.apm.agent.core.plugin.PluginResourcesResolver;
import org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.List;

/**
 * @author luoyonghua
 * @since 2020-03-19 23:58
 */
public class PluginsLoadTest {

    @Test
    public void loadPluginsTest() throws Exception {
        AgentClassLoader.initDefaultLoader();
        PluginResourcesResolver resolver = new PluginResourcesResolver();
        List<URL> resources = resolver.getResources();
    }

    @Test
    public void bootstrap() throws Exception {
        new PluginBootstrap().loadPlugins();
    }
}
