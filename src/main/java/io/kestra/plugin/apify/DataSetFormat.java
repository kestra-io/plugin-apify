package io.kestra.plugin.apify;

public enum DataSetFormat {
    JSON("json"),
    JSONL("jsonl"),
    XML("xml"),
    CSV("csv"),
    XLSX("xlsx"),
    RSS("rss");
    DataSetFormat(String format) {

    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}