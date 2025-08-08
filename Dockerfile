FROM camunda/camunda-bpm-platform:7.10.0

# Download MariaDB JDBC driver
RUN wget -O /camunda/lib/mariadb-java-client.jar https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.1.4/mariadb-java-client-3.1.4.jar
