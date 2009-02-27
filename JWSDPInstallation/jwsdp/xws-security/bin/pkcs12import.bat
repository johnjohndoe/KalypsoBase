@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Environment Variable Prequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem ---------------------------------------------------------------------------

rem Get standard environment variables
set PRG=%0%
if exist %PRG%\..\..\jwsdp-shared\bin\setenv.bat goto gotCmdPath
rem %0 must have been found by DOS using the %PATH% so we assume that setenv.bat
rem will also be found in the %PATH%
call setenv.bat
goto doneSetenv
:gotCmdPath
call %PRG%\..\..\jwsdp-shared\bin\setenv.bat
:doneSetenv

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome

rem Get command line arguments and save them
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

set JWSDP_HOME=%PRG%\..\..\..
set JAXRPC_SEC_LIB_HOME=%JWSDP_HOME%\xws-security\lib
set CLASSPATH=%JAXRPC_SEC_LIB_HOME%\pkcs12import.jar

rem Execute the target
"%JAVA_HOME%\bin\java.exe" -classpath %PRG%\..;"%CLASSPATH%";"%PATH%" com.sun.xml.wss.tools.PKCS12Import %CMD_LINE_ARGS%

:end
