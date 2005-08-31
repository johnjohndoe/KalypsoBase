@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Script for launching xjc usng the Launcher
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem $Id: xjc.bat,v 1.11 2004/05/14 19:10:58 kk122374 Exp $
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

rem Set JAXB_HOME variable
set JAXB_HOME="%PRG%"\..\jaxb

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined.
echo This environment variable is needed to run this program.
goto end
:gotJavaHome

rem Capture options
rem ===============
set xOpts=
:addOption
if ""%1x""==""x"" goto endOptions
set xOpts=%xOpts% %1
shift
goto addOption
:endOptions

rem  Execute the Launcher using the "xjc" target
"%JAVA_HOME%\bin\java.exe" -classpath "%PRG%";%PRG%\..;"%PRG%"\..\..\..\jwsdp-shared\bin;"%PATH%" LauncherBootstrap -verbose xjc  -DJAXB_HOME="%JAXB_HOME%" -DJAVA_HOME="%JAVA_HOME%" %xOpts%

:end
