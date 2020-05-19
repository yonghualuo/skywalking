package org.apache.skywalking.apm.plugin.dubbo.transport;

import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.transport.AbstractClient;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.util.CommonUtils;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Objects;
import org.apache.skywalking.apm.agent.core.constants.DevopsConstant;

/**
 * @author luoyonghua
 * @since 2020-05-09 17:09
 */
public class DubboClientInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes, MethodInterceptResult methodInterceptResult) throws Throwable {
        // pass
    }

    /**
     * mesh debug时，修改dubbo调用时建立连接的host为pod ip.
     */
    @Override
    public Object afterMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes, Object o) throws Throwable {
        if (enhancedInstance instanceof AbstractClient
            && Objects.equals(Boolean.TRUE.toString(), CommonUtils.getPodVar(DevopsConstant.MESH_FLAG))) {
            AbstractClient client = (AbstractClient)enhancedInstance;
            return new InetSocketAddress(client.getUrl().getParameter(DevopsConstant.MESH_POD_IP,
                NetUtils.filterLocalHost(client.getUrl().getHost())), client.getUrl().getPort());
        }

        return o;
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes, Throwable throwable) {
        // pass
    }
}
