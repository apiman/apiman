FROM adoptopenjdk/openjdk11:alpine-jre

ADD maven/target/${project.artifactId}-${project.version}-shaded.jar /opt/apiman/apiman-migration-assistant.jar

ENTRYPOINT ["java", "-jar", "/opt/apiman/apiman-migration-assistant.jar"]
