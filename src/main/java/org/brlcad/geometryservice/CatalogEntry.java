package org.brlcad.geometryservice;

import java.util.Map;
import java.util.TreeMap;

/**
 * Information about versioned geometry
 */
public class CatalogEntry {

    private final String name;
    private final String version;
    private final Map<String, String> metaData;

    public CatalogEntry(String name, String version, Map<String, String> metaData) {
        this.name = name;
        this.version = version;
        this.metaData = new TreeMap<String, String>(metaData);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }
}
