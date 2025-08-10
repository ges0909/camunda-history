# Camunda History Tests

## Testcontainer

|                | Deployment                              |
|----------------|-----------------------------------------|
| Camunda Engine | Testcontainer                           |
| Database       | Embedded H2 as default in Testcontainer |

```bash
./mvnw test -Dtest=TestcontainerCamundaEngineTest
```

## Docker

|                | Deployment       |
|----------------|------------------|
| Camunda Engine | Docker container |
| Database       | Docker container |

```bash
docker compose up -d
./mvnw test -Dtest=DockerizedCamundaEngineTest
docker compose down -v
   ```

## Embedded

|                | Deployment       |
|----------------|------------------|
| Camunda Engine | Embedded as JAR  |
| Database       | Docker container |

```bash
docker-compose up -d mariadb
until docker-compose exec mariadb mysqladmin ping -h localhost --silent
  do echo "Waiting for MariaDB..."
  sleep 2
done
echo "MariaDB is ready!"
./mvnw test -Dtest=EmbeddedCamundaEngineTest
docker compose down -v
```

## Docker

```bash
docker compose exec mariadb bash
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
