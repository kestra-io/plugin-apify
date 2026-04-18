# Kestra Apify Plugin

## What

- Provides plugin components under `io.kestra.plugin.apify`.
- Includes classes such as `ApifyConnection`, `DataSetFormat`, `ApifyTempFileRuntimeException`, `ApifySortDirection`.

## Why

- This plugin integrates Kestra with Apify Actor.
- It provides tasks that run Apify Actors with configurable inputs, limits, and webhooks.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `apify`

Infrastructure dependencies (Docker Compose services):

- `app`

### Key Plugin Classes

- `io.kestra.plugin.apify.ApifyConnection`
- `io.kestra.plugin.apify.actor.Run`
- `io.kestra.plugin.apify.dataset.Get`
- `io.kestra.plugin.apify.dataset.GetLastRun`
- `io.kestra.plugin.apify.dataset.Save`

### Project Structure

```
plugin-apify/
├── src/main/java/io/kestra/plugin/apify/dataset/
├── src/test/java/io/kestra/plugin/apify/dataset/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
