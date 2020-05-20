package org.apache.skywalking.apm.agent.core.context;

import java.util.function.Consumer;

/**
 * @author luoyonghua
 * @since 2020-05-19 14:58
 */
public class SW6CorrelationCarrierItem extends CarrierItem {

    public static final String HEADER_NAME = "sw6-correlation";
    private final CorrelationContext correlationContext;

    public SW6CorrelationCarrierItem(CorrelationContext correlationContext, CarrierItem next) {
        super(HEADER_NAME, correlationContext.serialize(), next);
        this.correlationContext = correlationContext;
    }

    @Override
    public void setHeadValue(String headValue) {
        this.correlationContext.deserialize(headValue);
    }
}
