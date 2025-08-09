# Camunda History Tests

## Camunda Engine as test container with embedded default database H2

## Camunda Engine and MariaDB both as externalized docker containers

```bash
docker compose up -d
```

## Embedded Camunda Engine and MariaDB as externalized docker container

```bash
docker compose up -d mariadb
```

## Erkenntnisse

camunda/camunda-bpm-platform:latest:

> "message": "ENGINE-09005 Could not parse BPMN process. Errors: \n* ENGINE-12018 History Time To Live (TTL) cannot be
> null. TTL is necessary for the History Cleanup to work. The following options are possible:\n* Set historyTimeToLive
> in the model\n* Set a default historyTimeToLive as a global process engine configuration\n* (Not recommended)
> Deactivate the enforceTTL config to disable this check: ENGINE-12018 History Time To Live (TTL) cannot be null. TTL is
> necessary for the History Cleanup to work. The following options are possible:\n* Set historyTimeToLive in the
> model\n* Set a default historyTimeToLive as a global process engine configuration\n* (Not recommended) Deactivate the
> enforceTTL config to disable this check | resource test-process.bpmn | line 3 | column 54",

## Voraussetzungen

Die Tests verwenden den Camunda Container aus Docker Compose anstatt eigene Testcontainer zu starten.

## Setup

1. **Docker Compose starten:**
   ```bash
   docker-compose up -d
   ```

2. **Warten bis Camunda bereit ist:**
    - Camunda ist unter http://localhost:8080 erreichbar
    - Die Engine REST API ist unter http://localhost:8080/engine-rest verfügbar

3. **Tests ausführen:**
   ```bash
   mvn test
   ```

## Wichtige Hinweise

- Der Docker Compose Container muss vor den Tests gestartet werden
- Die Tests verwenden Port 8080 (wie in docker-compose.yml konfiguriert)
- Nach den Tests kann der Container mit `docker-compose down` gestoppt werden

## Container-Status prüfen

```bash
# Container-Status anzeigen
docker-compose ps

# Logs anzeigen
docker-compose logs camunda
```