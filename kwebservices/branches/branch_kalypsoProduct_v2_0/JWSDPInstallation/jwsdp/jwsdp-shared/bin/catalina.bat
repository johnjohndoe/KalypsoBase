@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Start/Stop Script for the CATALINA Server
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem $Id: catalina.bat,v 1.5 2005/04/18 19:43:21 rameshm Exp $
rem ---------------------------------------------------------------------------

rem Get standard environment variables
set PRG=%0
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

if not "%CATALINA_HOME%" == "" goto gotCATALINA_HOME
echo The CATALINA_HOME environment variable is not defined
echo This environment variable is needed to run this program
echo Please check <TOMCAT_InstallDir>\jwsdp-shared\bin\setenv.bat file
echo to see if CATALINA_HOME variable is defined 
goto end
:gotCATALINA_HOME

if ""%1""=="""" goto usage 
goto getArgs
:usage
echo Usage:  catalina ( commands ... )
echo commands:
echo   debug             Start Catalina in a debugger
echo   embedded          Start Catalina in embedded mode
echo   jpda start        Start Catalina under JPDA debugger
echo   run               Start Catalina in the current window
echo   start             Start Catalina in a separate window
echo   stop              Stop Catalina
goto end

:getArgs
rem Get command line arguments and save them
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute the Tomcat launcher
"%JAVA_HOME%\bin\java.exe" -Djava.endorsed.dirs="%CATALINA_HOME%\jaxp\lib;%CATALINA_HOME%\jaxp\lib\endorsed" -classpath "%CATALINA_HOME%\jwsdp-shared\bin;%CATALINA_HOME%\jwsdp-shared\bin\commons-launcher.jar;%CATALINA_HOME%\apache-ant\lib\ant.jar;%CATALINA_HOME%\apache-ant\lib\ant-launcher.jar;%CATALINA_HOME%\apache-ant\lib\ant-nodeps.jar" -DJAVA_HOME="%JAVA_HOME%" -DCATALINA_OPTS="%CATALINA_OPTS%" LauncherBootstrap jwsdp %CMD_LINE_ARGS%

:end
