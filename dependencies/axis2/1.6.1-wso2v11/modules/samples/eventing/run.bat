@echo off

REM  Licensed to the Apache Software Foundation (ASF) under one
REM  or more contributor license agreements. See the NOTICE file
REM  distributed with this work for additional information
REM  regarding copyright ownership. The ASF licenses this file
REM  to you under the Apache License, Version 2.0 (the
REM  "License"); you may not use this file except in compliance
REM  with the License. You may obtain a copy of the License at
REM  
REM  http://www.apache.org/licenses/LICENSE-2.0
REM  
REM  Unless required by applicable law or agreed to in writing,
REM  software distributed under the License is distributed on an
REM  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM  KIND, either express or implied. See the License for the
REM  specific language governing permissions and limitations
REM  under the License.

rem ---------------------------------------------------------------------------
rem Start script for running the Eventing Sample Client
rem
rem ---------------------------------------------------------------------------

rem store the current directory
set CURRENT_DIR=%cd%

rem check the AXIS2_HOME environment variable
if not "%AXIS2_HOME%" == "" goto gotHome

rem guess the home. Jump two directories up and take that as the home
cd ..
cd ..
set AXIS2_HOME=%cd%

echo using Axis Home %AXIS2_HOME%


:gotHome
if EXIST "%AXIS2_HOME%\lib\axis2*.jar" goto okHome
echo The AXIS2_HOME environment variable seems not to point to the correct loaction!
echo This environment variable is needed to run this program
pause
goto end

:okHome
cd %CURRENT_DIR%

setlocal EnableDelayedExpansion
rem loop through the libs and add them to the class path
set AXIS2_CLASS_PATH=%AXIS2_HOME%
FOR %%c in (%AXIS2_HOME%\lib\*.jar) DO set AXIS2_CLASS_PATH=!AXIS2_CLASS_PATH!;%%c

set AXIS2_CLASS_PATH=%AXIS2_CLASS_PATH%;"%CURRENT_DIR%\EventingSample.jar"

java -cp %AXIS2_CLASS_PATH% -Daxis2.repo=%AXIS2_HOME% sample.eventing.Client
endlocal
:end