@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Script for disabling Callback 
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem $Id$
rem ---------------------------------------------------------------------------

rem Get standard environment variables
set PRG=%0
if exist %PRG%\..\setenv.bat goto gotCmdPath
rem %0 must have been found by DOS using the %PATH% so we assume that setenv.bat
rem will also be found in the %PATH%
call setenv.bat
goto doneSetenv
:gotCmdPath
call %PRG%\..\setenv.bat
:doneSetenv

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome

if not "%JWSDP_HOME%" == "" goto gotJWSDPHOME
echo The JWSDP_HOME environment variable is not defined
echo This environment variable is needed to run this program
echo Please check <JWSDP_InstallDir>\jwsdp-shared\bin\setenv.bat file
echo to see if JWSDP_HOME variable is defined
goto end
:gotJWSDPHOME


rem Invoking CallbackControler for disabling callback 
"%JAVA_HOME%\bin\java.exe" -Djava.endorsed.dirs="%JWSDP_HOME%\jaxp\lib;%JWSDP_HOME%\jaxp\lib\endorsed" -classpath %JWSDP_HOME%\jwsdp-shared\webapps\jwsdp-callback\WEB-INF\classes;%PRG%\..;%PATH% com.sun.jwsdp.callback.CallbackController false 

:end
