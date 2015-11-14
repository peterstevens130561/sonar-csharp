
IF [%LoadedDevEnv%] == [] (
	SET LoadedDevEnv = "Loaded"
	CALL "C:\Program Files (x86)\Microsoft Visual Studio 14.0\Common7\Tools\VsDevCmd.bat"
)

IF [%1] == [] (
	ECHO "Usage : integrate <sonarlint> <sonar-csharp>"
	ECHO "sonarlint not  specified"
	exit 1
)
SET SONARLINT=%1
SET CSHARP=%2
SET CURDIR="%cd%"

SET MSBUILD="C:/Program Files (x86)/MSBuild/14.0/Bin/MSBuild.exe"
SET WINZIP="C:\Program Files\WinZip\WZZIP.EXE"
if NOT EXIST %WINZIP% (
	ECHO "WZZIP Not installed"
	EXIT 1
)

if NOT EXIST %SONARLINT% (
	ECHO %SONARLINT% does not exist
	exit /b 1
)
cd  %SONARLINT%
ECHO *** Building sonarlint
%MSBUILD%  SonarLint.sln /t:Clean,Rebuild /p:Configuration=Release /v:quiet /nologo
if %errorlevel% neq 0 (
	ECHO "**** BUILD FAILED"
	CD %CURDIR%
	exit /b %errorlevel%
)

ECHO  **** Zipping SonarQube.SonarLint.Runner
cd src/BHI.SonarQube.SonarLint.Runner/bin/Release
%WINZIP% SonarLint.Runner.zip *
if %errorlevel% neq 0 (
	ECHO **** Zipping failed
	CD %CURDIR%
	exit /b %errorlevel%
)

COPY SonarLint.Runner.zip %CURDIR%\%CSHARP%\src\main\resources\SonarLint.Runner.Zip
if %errorlevel% neq 0 exit /b %errorlevel%

ECHO ***** Creating rule files
REM zip the SonarLint.Runner dir, into the sonar-csharp dir
cd %CURDIR%\%SONARLINT%\src\BHI.SonarLint.Descriptor\bin\Release
BHI.SonarLint.Descriptor rules.xml profile.xml sqale.xml
if %errorlevel% neq 0 (
	ECHO "**** ?Creating rule files failed"
	CD %CURDIR%
	exit /b %errorlevel%
)
ROBOCOPY .  %CURDIR%\%CSHARP%\src\main\resources\org\sonar\plugins\csharp rules.xml profile.xml sqale.xml

ECHO **** Buuilding sonar-csharp
cd %CURDIR%\%CSHARP%
call mvn clean install -q -DskipTests=true
ECHO **** Done building
if %errorlevel% neq 0 (
	echo *** COULD NOT BUILD SONAR-CSHARP
	CD %CURDIR%
	exit /b %errorlevel%
)

cd %CURDIR%\%CSHARP%
ECHO **** Installing sonar-charp
COPY target\sonar-csharp-plugin.jar "C:\Program Files\sonarqube-5.1.2\extensions\plugins\sonar-csharp-plugin.jar"
if %errorlevel% neq 0 (
	echo *** COULD NOT INSTALL SONAR-CSHARP
	CD %CURDIR%
	exit /b %errorlevel%
)