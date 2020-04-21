package org.apache.skywalking.apm.plugin.dubbo.schema;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassStaticMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * @author luoyonghua
 * @since 2020-04-21 11:49
 */
public class DubboBeanDefinitionInstrumentation extends ClassStaticMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "com.alibaba.dubbo.config.spring.schema.DubboBeanDefinitionParser";
    private static final String INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.dubbo.schema.DubboBeanDefinitionInterceptor";

    @Override
    protected ClassMatch enhanceClass() {
        return NameMatch.byName(ENHANCE_CLASS);
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[] {
            new StaticMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("parse");
                }

                @Override
                public String getMethodsInterceptor() {
                    return INTERCEPT_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }
}