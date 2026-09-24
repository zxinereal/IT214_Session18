@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%
@if "%JAVA_HOME%" == "" (
  for /f "tokens=2 delims==" %%i in ('set JAVA_HOME 2^>nul') do set "JAVA_HOME=%%i"
)
@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post batch processing, a setlocal declare Token is used here
setlocal

@REM %~dp0 is expanded pathname of the current script under NT
set DEFAULT_MAVEN_PROJECT_BASEDIR=%~dp0
set DEFAULT_MAVEN_PROJECT_BASEDIR=%DEFAULT_MAVEN_PROJECT_BASEDIR:~0,-1%

set MAVEN_PROJECT_BASEDIR=%DEFAULT_MAVEN_PROJECT_BASEDIR%

@REM Find maven.config file
set MAVEN_CONFIG=%MAVEN_PROJECT_BASEDIR%\.mvn\maven.config

@REM Execute Maven Wrapper Script
set WRAPPER_JAR=%MAVEN_PROJECT_BASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%MAVEN_PROJECT_BASEDIR%\.mvn\wrapper\maven-wrapper.properties

if exist "%WRAPPER_JAR%" goto run

set DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
powershell -Command "if ($PSVersionTable.PSVersion.Major -ge 3) { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%WRAPPER_JAR%'"

:run
set JAVA_CMD=java
if not "%JAVA_HOME%" == "" (
  if exist "%JAVA_HOME%\bin\java.exe" set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
)

"%JAVA_CMD%" -classpath "%WRAPPER_JAR%" "-Dmaven.home=%MAVEN_PROJECT_BASEDIR%\.mvn\wrapper" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECT_BASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*


if ERRORLEVEL 1 set ERROR_CODE=1

cmd /C exit /B %ERROR_CODE%
