set -x
# arg1: place of sonrlint
# arg2: place of sonar-sharp
SET SONARLINT=$0
SET CSHARP=$1
SET CURDIR="%cd%"

SET MSBUILD="C:/Program Files (x86)/MSBuild/14.0/Bin/MSBuild.exe"
# build sonarlint
cd $CSHARP
"${MSBUILD}"  SonarLint.sln '/t:Clean'
cd src/SonarQube.SonarLint.Runner/bin/Release

# zip the SonarLint.Runner dir, into the sonar-csharp dir

