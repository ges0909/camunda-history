# Camunda History Tests

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