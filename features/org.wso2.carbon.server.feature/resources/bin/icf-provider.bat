@echo off

REM ---------------------------------------------------------------------------
REM   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM   Licensed under the Apache License, Version 2.0 (the "License");
REM   you may not use this file except in compliance with the License.
REM   You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.

rem ----- if JAVA_HOME is not set we're not happy ------------------------------
:checkJava

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto checkServer

:noJavaHome
echo "You must set the JAVA_HOME variable before running CARBON."
goto end

rem ----- Only set CARBON_HOME if not already set ----------------------------
:checkServer
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%CARBON_HOME%"=="" set CARBON_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET wsasDrive=%CARBON_HOME:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:

rem find CARBON_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if not exist "%CARBON_HOME%\bin\kernel-version.txt" goto noServerHome

goto commandLifecycle

:noServerHome
echo CARBON_HOME is set incorrectly or CARBON could not be located. Please set CARBON_HOME.
goto end

:commandLifecycle
goto findJdk

:findJdk

set CMD=RUN %*

:checkJdk16
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr /r "1.[8]" >NUL
IF ERRORLEVEL 1 goto unknownJdk
goto jdk16

:unknownJdk
echo Starting WSO2 Carbon (in unsupported JDK)
echo [ERROR] CARBON is supported only on JDK 1.8
goto jdk16

:jdk16
goto runTool

:runTool

set CURRENT_DIR=%cd%

cd %CARBON_HOME%\bin
echo JAVA_HOME environment variable is set to %JAVA_HOME%
echo CARBON_HOME environment variable is set to %CARBON_HOME%
java -cp ".\*;..\bin\tools\*" -Dcarbon.home="%CARBON_HOME" -Dwso2.carbon.tool="icf-provider" org.wso2.carbon.tools.CarbonToolExecutor %1 %2 %3 %4

:end
goto endlocal

:endlocal

:END
