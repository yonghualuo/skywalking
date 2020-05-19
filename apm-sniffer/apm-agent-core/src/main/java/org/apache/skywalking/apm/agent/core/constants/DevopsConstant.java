package org.apache.skywalking.apm.agent.core.constants;

/**
 * @author luoyonghua
 * @since 2020-04-21 13:52
 */
public final class DevopsConstant {
    public static final String LABEL_KEY = "devops.env.label";
    public static final String FORCE_USE_LABEL = "dubbo.force.env.label";
    public static final String LABEL_ENV_SEPARATOR = ".";
    public static final String MESH_FLAG = "devops.mesh.on";
    public static final String MESH_POD_IP = "devops.mesh.host";
    public static final String VIRTUAL_ENV_HTTP_HEADER = "X-Virtual-Env";
}
