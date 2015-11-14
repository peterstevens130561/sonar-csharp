@Echo on
# arg1: place of sonrlint
# arg2: place of sonar-sharp
SET SONARLINT=%1
SET CSHARP=%2
SET CURDIR="%cd%"

SET MSBUILD="C:/Program Files (x86)/MSBuild/14.0/Bin/MSBuild.exe"
SET WINZIP="C:\Program Files\WinZip\WZZIP.EXE"
REM build sonarlint
cd  %SONARLINT%
REM %MSBUILD%  SonarLint.sln /t:Clean,Rebuild
cd src/SonarQube.SonarLint.Runner/bin/Release
%WINZIP% SonarLint.Runner.zip *
COPY SonarLint.Runner.zip %CURDIR%\%CSHARP%\src\main\resources\SonarLint.Runner.Zip
REM zip the SonarLint.Runner dir, into the sonar-csharp dir
cd %CURDIR%\%SONARLINT%\src\BHI.SonarLint.Descriptor\bin\Release
BHI.SonarLint.Descriptor rules.xml profile.xml sqale.xml
ROBOCOPY .  %CURDIR%\%CSHARP%\src\main\resources\org\sonar\plugins\csharp rules.xml profile.xml sqale.xml
cd %CURDIR%\%CSHARP%
mvn clean install -DskipTests=true
COPY target/sonar-csharp-plugin.jar "C:/Program Files/SonarQube/sonarqube-5.1.2/extensions/plugins