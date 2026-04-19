# Kestra Apify Plugin

## What

- Provides plugin components under `io.kestra.plugin.apify`.
- Includes classes such as `ApifyConnection`, `DataSetFormat`, `ApifyTempFileRuntimeException`, `ApifySortDirection`.

## Why

- What user problem does this solve? Teams need to orchestrate Apify Actors and datasets from Kestra from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Apify steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Apify.

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
