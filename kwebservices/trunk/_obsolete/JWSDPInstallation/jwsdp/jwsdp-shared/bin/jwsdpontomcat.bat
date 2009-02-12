@echo off

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Script to copy JWSDP files into a Apache Jakarta Tomcat 5.x installation
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem $Id: jwsdpontomcat.bat,v 1.4 2005/06/02 09:41:01 rameshm Exp $
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

rem Check arguments
if not ""%1"" == """" goto gotCatalinaHomeArg
echo Missing argument
echo Usage: %PRG% Tomcat5-installation-directory <target>
goto end
:gotCatalinaHomeArg
set CATALINA_HOME=%1
if exist "%CATALINA_HOME%\server\lib\catalina.jar" goto okCatalinaArg
echo Directory does not contain a Apache Jakarta Tomcat 5 installation
echo Usage: %PRG% Tomcat5-installation-directory" <target>
goto end
:okCatalinaArg
shift

rem Get command line arguments and save them
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute the Launcher using the "ant" target by giving jwsdpontomcat.xml
rem as build file
"%JAVA_HOME%\bin\java.exe" -Djava.endorsed.dirs="%JWSDP_HOME%\jaxp\lib;%JWSDP_HOME%\jaxp\lib\endorsed" -classpath "%JWSDP_HOME%\jwsdp-shared\bin;%JWSDP_HOME%\jwsdp-shared\bin\commons-launcher.jar;%JWSDP_HOME%\apache-ant\lib\ant.jar;%JWSDP_HOME%\apache-ant\lib\ant-launcher.jar;%JWSDP_HOME%\apache-ant\lib\ant-nodeps.jar;%JWSDP_HOME%\jaxp\lib\jaxp-api.jar;%JWSDP_HOME%\jaxp\lib\endorsed\sax.jar;%JWSDP_HOME%\jaxp\lib\endorsed\dom.jar;%JWSDP_HOME%\jaxp\lib\endorsed\xercesImpl.jar;%JWSDP_HOME%\jaxp\lib\endorsed\xalan.jar" LauncherBootstrap -verbose ant -buildfile "%JWSDP_HOME%\jwsdp-shared\bin\jwsdpontomcat.xml"  -Dcatalina.home="%CATALINA_HOME%" %CMD_LINE_ARGS%


:end
