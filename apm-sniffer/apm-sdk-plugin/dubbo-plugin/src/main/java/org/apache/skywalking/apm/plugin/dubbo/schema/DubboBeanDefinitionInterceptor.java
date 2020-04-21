package org.apache.skywalking.apm.plugin.dubbo.schema;

import com.alibaba.dubbo.config.spring.ServiceBean;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedMap;

import java.lang.reflect.Method;

import static org.apache.skywalking.apm.plugin.dubbo.Constants.LABEL_KEY;

/**
 * 拦截DubboBeanDefinition解析, 添加自定义env变量
 * @author luoyonghua
 * @since 2020-04-21 11:50
 */
public class DubboBeanDefinitionInterceptor implements StaticMethodsAroundInterceptor {

    private static final ILog logger = LogManager.getLogger(DubboBeanDefinitionInterceptor.class);

    @Override
    public void beforeMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, MethodInterceptResult result) {
        // pass
    }

    @Override
    public Object afterMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Object ret) {
        if (null != ret) {
            String label = System.getProperty(LABEL_KEY);
            if (null == label) {
                return ret;
            }

            try {
                BeanDefinition beanDefinition = (BeanDefinition) ret;
                BeanWrapperImpl beanWrapper = new BeanWrapperImpl();
                beanWrapper.setBeanInstance(beanDefinition);
                Object beanClass = beanWrapper.getPropertyValue("beanClass");
                if (null != beanClass && ServiceBean.class.equals(beanClass)) {
                    PropertyValue propertyValue = beanDefinition.getPropertyValues().getPropertyValue("parameters");
                    if (null != propertyValue && propertyValue.getValue() instanceof ManagedMap) {
                        ManagedMap managedMap = (ManagedMap) propertyValue.getValue();
                        // put env label
                        managedMap.put(LABEL_KEY, new TypedStringValue(label, String.class));
                    } else {
                        ManagedMap managedMap = new ManagedMap();
                        managedMap.put(LABEL_KEY, new TypedStringValue(label, String.class));
                        beanDefinition.getPropertyValues().addPropertyValue("parameters", managedMap);
                    }
                }
            } catch (Throwable t) {
                logger.error(t, "dubbo service bean env label init error");
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Throwable t) {
        // pass
    }
}
