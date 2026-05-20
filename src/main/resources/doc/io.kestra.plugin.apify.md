# How to use the Apify plugin

Run Apify actors and tasks, and retrieve dataset results from Kestra flows.

## Authentication

Set `apiToken` (required) to your Apify API token. Store secrets in [secrets](https://kestra.io/docs/concepts/secret) and apply connection properties globally with [plugin defaults](https://kestra.io/docs/workflow-components/plugin-defaults).

## Tasks

`actor.Run` starts an Apify actor run — set `actorId` (required). Optionally pass `input` (a map of actor input parameters), `memory` (one of `MB_128` through `MB_32768`), `requestTimeout` (seconds), `maxItems`, `maxTotalChargeUsd`, `build`, `webhooks`, and `waitForFinish` (seconds to wait, 0–60, before returning). The output is a full `ActorRun` object including `id`, `status`, `defaultDatasetId`, `startedAt`, `finishedAt`, and usage fields.

`task.Run` starts an Apify saved task run — set `taskId` (required). Accepts the same optional properties as `actor.Run`.

`dataset.Get` retrieves items from an Apify dataset — set `datasetId` (required). Control the result with `offset` (default 0), `limit` (default 1000), `fields` / `omit` (field filter lists), `clean` (default `true`), `sort` (default `ASC`), `flatten`, `skipEmpty` (default `true`), `skipHidden` (default `false`), `simplified` (default `false`), and `skipFailedPages` (default `false`). The output includes `dataset` (list of items).

`dataset.GetLastRun` retrieves the dataset from the last run of an actor — set `actorId` (required). Returns a full `ActorRun` object.
