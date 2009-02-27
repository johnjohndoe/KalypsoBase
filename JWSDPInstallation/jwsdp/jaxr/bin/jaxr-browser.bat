@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Script to run jaxr-browser
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem $Id: jaxr-browser.bat,v 1.5 2004/03/30 23:28:21 ofung Exp $
rem ---------------------------------------------------------------------------

rem Get standard environment variables
set PRG=%0
if exist %PRG%\..\..\..\jwsdp-shared\bin\setenv.bat goto gotCmdPath
rem %0 must have been found by DOS using the %PATH% so we assume that setenv.bat
rem will also be found in the %PATH%
call setenv.bat
goto doneSetenv
:gotCmdPath
call %PRG%\..\..\..\jwsdp-shared\bin\setenv.bat
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

rem Execute the launcher with jaxr-browser target
"%JAVA_HOME%\bin\java.exe" -classpath %PRG%\..;%PRG%\..\..\..\jwsdp-shared\bin;"%PATH%" LauncherBootstrap -verbose jaxr-browser %CMD_LINE_ARGS%

:end
