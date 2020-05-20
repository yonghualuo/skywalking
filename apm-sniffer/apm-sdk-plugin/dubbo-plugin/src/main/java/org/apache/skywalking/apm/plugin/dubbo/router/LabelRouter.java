package org.apache.skywalking.apm.plugin.dubbo.router;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;

import org.apache.skywalking.apm.agent.core.context.*;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.util.CommonUtils;
import org.apache.skywalking.apm.agent.core.util.StringUtils;
import org.apache.skywalking.apm.agent.core.util.ThreadLocalLabel;
import org.apache.skywalking.apm.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.skywalking.apm.agent.core.constants.DevopsConstant.*;

/**
 * @author luoyonghua
 * @since 2020-04-20 21:22
 */
public class LabelRouter implements Router {

    private static final ILog logger = LogManager.getLogger(LabelRouter.class);

    private static final int DEFAULT_PRIORITY = 500;
    private int priority;
    private URL url;
    private static final URL ROUTER_URL = new URL("label", Constants.ANYHOST_VALUE, 0, Constants.ANY_VALUE).addParameters(Constants.RUNTIME_KEY, "true");

    public LabelRouter() {
        this.url = ROUTER_URL;
        this.priority = url.getParameter(Constants.PRIORITY_KEY, DEFAULT_PRIORITY);
    }

    @Override
    public URL getUrl() {
        return url;
    }

    /**
     * 从调用链上下文中获取环境标签
     *
     * @param labelKey
     * @return
     */
    private String getLabelFromTraceContext(String labelKey) {
        String label = null;
        CorrelationContext correlationContext = ContextManager.getCorrelationContext();
        if (correlationContext != null && correlationContext.get(labelKey).isPresent()) {
            label = correlationContext.get(labelKey).get();
        }

        if (StringUtil.isEmpty(label)) {
            label = RpcContext.getContext().getAttachment(labelKey);
        }

        if (StringUtil.isEmpty(label)) {
            label = CommonUtils.getPodVar(labelKey);
        }

        return label;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        String label = getLabelFromTraceContext(LABEL_KEY);

        // filter
        List<Invoker<T>> result = new ArrayList<Invoker<T>>();
        // Dynamic param
        if (!StringUtils.isEmpty(label)) {
            // Select label invokers first
            // exactly match
            String[] specLabels = StringUtils.split(label, LABEL_ENV_SEPARATOR);
            List<String> specLabelList = new ArrayList<String>(Arrays.asList(specLabels));

            while (!specLabelList.isEmpty() && result.isEmpty()) {
                String matchLabel = StringUtils.join(specLabelList, LABEL_ENV_SEPARATOR);
                for (Invoker<T> invoker : invokers) {
                    if (matchLabel.equals(invoker.getUrl().getParameter(LABEL_KEY))) {
                        result.add(invoker);
                    }
                }
                specLabelList.remove(specLabelList.size() - 1);
            }
        }

        // If LABEL_KEY unspecified or no invoker be selected, downgrade to normal invokers
        if (result.isEmpty()) {
            // Only forceTag = true force match, otherwise downgrade
                String forceTag = getLabelFromTraceContext(FORCE_USE_LABEL);
            if (StringUtils.isEmpty(forceTag) || "false".equals(forceTag)) {
                for (Invoker<T> invoker : invokers) {
                    if (StringUtils.isEmpty(invoker.getUrl().getParameter(LABEL_KEY))) {
                        result.add(invoker);
                    }
                }
            }
        }

        if (result.isEmpty()) {
            logger.warn("[DubboRouteEmpty] the label is {}, the provider path is {}.", label, url.getPath());
        }

        return result;
    }

    /**
     */
    @Override
    public int compareTo(Router o) {
        if (o != null && o.getClass() == LabelRouter.class) {
            LabelRouter router = (LabelRouter) o;
            return (this.getPriority() < router.getPriority()) ? -1 : ((this.getPriority() == router.getPriority()) ? 0 : 1);
        } else {
            return 1;
        }
    }

    public int getPriority() {
        return priority;
    }

    /**
     * 获取环境标签的逻辑如下：
     * 1) RPC上下文
     * 2) 线程上下文
     * 3) 系统环境变量
     */
    private static List<EnvLabel> envLabels = new ArrayList<>();

    {
        envLabels.add(labelKey -> RpcContext.getContext().getAttachment(labelKey));
        envLabels.add(labelKey -> {
            String label = ThreadLocalLabel.get(labelKey);
            if (!StringUtils.isEmpty(label)) {
                RpcContext.getContext().getAttachments().put(labelKey, label);
            }
            return label;
        });
        envLabels.add(labelKey ->  {
            String label = CommonUtils.getPodVar(labelKey);
            if (!StringUtils.isEmpty(label)) {
                ThreadLocalLabel.set(labelKey, label);
                RpcContext.getContext().getAttachments().put(labelKey, label);
            }
            return label;
        });
    }

    public String getLabelFromEnv(String labelKey) {
        for (EnvLabel envLabel : envLabels) {
            String label = envLabel.get(labelKey);
            if (!StringUtils.isEmpty(label)) {
                return label;
            }
        }

        return null;
    }

    public interface EnvLabel {
        String get(String labelKey);
    }
}
