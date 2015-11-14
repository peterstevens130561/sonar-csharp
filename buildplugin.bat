SET DEST="C:\Program Files\sonarqube-5.1.2\extensions\plugins"
call mvn clean install -DskipTests=true
COPY target\sonar-csharp-plugin.jar %DEST%