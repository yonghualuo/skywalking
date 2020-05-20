package org.apache.skywalking.apm.agent.core.context;

import org.apache.skywalking.apm.agent.core.base64.Base64;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author luoyonghua
 * @since 2020-05-19 14:18
 */
public class CorrelationContext {

    private final Map<String, String> data;

    public CorrelationContext() {
        this.data = new HashMap<>(Config.Correlation.ELEMENT_MAX_NUMBER);
    }

    public Optional<String> put(String key, String value) {
        // key must not null
        if (key == null) {
            return Optional.empty();
        }

        // remove and return previous value when value is empty
        if (StringUtil.isEmpty(value)) {
            return Optional.ofNullable(data.remove(key));
        }

        // check value length
        if (value.length() > Config.Correlation.VALUE_MAX_LENGTH) {
            return Optional.empty();
        }

        // already contain key
        if (data.containsKey(key)) {
            final String previousValue = data.put(key, value);
            return Optional.of(previousValue);
        }

        // check keys count
        if (data.size() >= Config.Correlation.ELEMENT_MAX_NUMBER) {
            return Optional.empty();
        }

        // setting
        data.put(key, value);
        return Optional.empty();
    }

    public Optional<String> get(String key) {
        if (key == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(data.get(key));
    }

    /**
     * Serialize this {@link CorrelationContext} to a {@link String}
     *
     * @return the serialization string.
     */
    public String serialize() {
        if (data.isEmpty()) {
            return "";
        }

        return data.entrySet().stream()
                .map(entry -> Base64.encode(entry.getKey()) + ":" + Base64.encode(entry.getValue()))
                .collect(Collectors.joining(","));
    }

    /**
     * Deserialize data from {@link String}
     */
    public void deserialize(String value) {
        if (StringUtil.isEmpty(value)) {
            return;
        }

        for (String perData : value.split(",")) {
            // Only data with limited count of elements can be added
            if (data.size() >= Config.Correlation.ELEMENT_MAX_NUMBER) {
                break;
            }
            final String[] parts = perData.split(":");
            if (parts.length != 2) {
                continue;
            }
            data.put(Base64.decode2UTFString(parts[0]), Base64.decode2UTFString(parts[1]));
        }
    }

    /**
     * Prepare for the cross-process propagation. Inject the {@link #data} into {@link ContextCarrier#getCorrelationContext()}
     */
    void inject(ContextCarrier carrier) {
        carrier.getCorrelationContext().data.putAll(this.data);
    }

    /**
     * Extra the {@link ContextCarrier#getCorrelationContext()} into this context.
     */
    void extract(ContextCarrier carrier) {
        final Map<String, String> carrierCorrelationContext = carrier.getCorrelationContext().data;
        for (Map.Entry<String, String> entry : carrierCorrelationContext.entrySet()) {
            // Only data with limited count of elements can be added
            if (data.size() >= Config.Correlation.ELEMENT_MAX_NUMBER) {
                break;
            }

            this.data.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Clone the context data, work for capture to cross-thread.
     */
    @Override
    public CorrelationContext clone() {
        final CorrelationContext context = new CorrelationContext();
        context.data.putAll(this.data);
        return context;
    }

    void continued(ContextSnapshot snapshot) {
        this.data.putAll(snapshot.getCorrelationContext().data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorrelationContext that = (CorrelationContext) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
