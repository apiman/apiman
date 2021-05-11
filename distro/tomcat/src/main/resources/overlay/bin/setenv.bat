:: Change file permissions so that log files can be read by all users (e.g. fluentd or logstash)
set "UMASK=0022"
:: See: https://logging.apache.org/log4j/2.x/log4j-appserver/index.html
set "CLASSPATH=%CATALINA_HOME%\log4j2\lib\*;%CATALINA_HOME%\log4j2\conf"
:: Set the log4j2 delegate in Apiman; use the logging config file that we create in our overlay.
:: This means we can control Tomcat and Apiman logging from the same place.
set "CATALINA_OPTS=%CATALINA_OPTS% -Dapiman.logger-delegate=log4j2 -Dlog4j.configurationFile=%CATALINA_HOME%\log4j2\conf\log4j2-tomcat.xml"
:: Force use of async logging, which will utilise lmax disruptor for higher performance logging.
:: See: https://logging.apache.org/log4j/2.x/manual/async.html
set "CATALINA_OPTS= %CATALINA_OPTS% -Dorg.apache.logging.log4j.core.async.AsyncLoggerContextSelector=log4j2.contextSelector"
exit /b 0
