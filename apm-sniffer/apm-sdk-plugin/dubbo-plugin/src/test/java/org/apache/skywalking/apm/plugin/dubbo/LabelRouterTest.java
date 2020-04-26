package org.apache.skywalking.apm.plugin.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;
import org.apache.skywalking.apm.plugin.dubbo.router.LabelRouter;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author luoyonghua
 * @since 2020-04-26 12:05
 */
public class LabelRouterTest {

    @Test
    public void getLabelFromEnvTest() throws Exception {
        RpcContext.getContext().getAttachments().put(Constants.LABEL_KEY, "");
        LabelRouter labelRouter = new LabelRouter();
        System.out.println(labelRouter.getLabelFromEnv(Constants.LABEL_KEY));

        ExecutorService executorService = Executors.newCachedThreadPool();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("[child thread] " + Thread.currentThread().getId() + " get " + labelRouter.getLabelFromEnv(Constants.LABEL_KEY) + " in Runnable");
            }
        };

        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                System.out.println("[child thread] " + Thread.currentThread().getId() + " get " + labelRouter.getLabelFromEnv(Constants.LABEL_KEY) + " in Runnable");
            }
        };

        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task2);

        Thread.sleep(5000);
        executorService.shutdown();
    }
}
