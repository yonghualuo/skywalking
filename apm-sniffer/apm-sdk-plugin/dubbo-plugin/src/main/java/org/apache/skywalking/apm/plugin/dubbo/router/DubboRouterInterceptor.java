package org.apache.skywalking.apm.plugin.dubbo.router;

import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.router.MockInvokersSelector;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author luoyonghua
 * @since 2020-04-20 21:32
 */
public class DubboRouterInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        // pass
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        if (objInst instanceof RegistryDirectory && null != ((RegistryDirectory) objInst).getRouters()) {
            List<Router> routers = ((RegistryDirectory) objInst).getRouters();
            if (!routers.isEmpty() && routers.get(routers.size() - 1) instanceof MockInvokersSelector) {
                routers.add(routers.size() - 1, new LabelRouter());
            } else {
                routers.add(new LabelRouter());
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        // pass
    }
}
