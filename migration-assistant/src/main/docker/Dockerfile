FROM eclipse-temurin:11-jre-alpine

ADD maven/target/${project.artifactId}-${project.version}-shaded.jar /opt/apiman/apiman-migration-assistant.jar

ENTRYPOINT ["java", "-jar", "/opt/apiman/apiman-migration-assistant.jar"]
