package eu.okaeri.platform.core.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public abstract class Document {

    private Map<String, Object> data;

    public Document() {
        this.data = new LinkedHashMap<>();
    }

    public Document put(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
