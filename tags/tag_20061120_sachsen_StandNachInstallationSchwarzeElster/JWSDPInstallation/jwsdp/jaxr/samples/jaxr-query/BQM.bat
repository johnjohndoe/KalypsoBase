REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

rem Get standard environment variables
set PRG=%0
if exist %PRG%\..\..\..\..\jwsdp-shared\bin\setenv.bat goto gotCmdPath
rem %0 must have been found by DOS using the %PATH% so we assume that setenv.bat
rem will also be found in the %PATH%
call setenv.bat
goto doneSetenv
:gotCmdPath
call %PRG%\..\..\..\..\jwsdp-shared\bin\setenv.bat
:doneSetenv

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome


set JWSDP_HOME=..\..\..
set JAXR_HOME=..\..
set JAXR_LIB=%JAXR_HOME%\lib
set JAXP_HOME=..\..\..\jaxp
set SAAJ_HOME=..\..\..\saaj
set JAXB_HOME=..\..\..\jaxb

set CLASSPATH=%JAXR_LIB%\soap.jar;%JAXP_HOME%\lib\endorsed\xercesImpl.jar;%JAXR_LIB%\jaxr-api.jar;%JWSDP_HOME%\jwsdp-shared\lib\mail.jar;%JWSDP_HOME%\jwsdp-shared\lib\activation.jar;%JAXP_HOME%\lib\endorsed\xalan.jar;%JWSDP_HOME%\jwsdp-shared\lib\jsse.jar;%JWSDP_HOME%\jwsdp-shared\lib\jcert.jar;%JWSDP_HOME%\jwsdp-shared\lib\jnet.jar;%JWSDP_HOME%\jwsdp-shared\lib\jaas.jar;%JAXR_LIB%\jaxr-impl.jar;%SAAJ_HOME%\lib\saaj-api.jar;%SAAJ_HOME%\lib\saaj-impl.jar;%JAXP_HOME%\lib\jaxp-api.jar;%JAXP_HOME%\lib\endorsed\dom.jar;%JAXP_HOME%\lib\endorsed\sax.jar;%JWSDP_HOME%\jwsdp-shared\lib\commons-logging.jar;%SAAJ_HOME%\lib\dom4j.jar;%JAXR_HOME%\samples\jaxr-query;%JAXB_HOME%\lib\jaxb-api.jar;%JAXB_HOME%\lib\jaxb-impl.jar;%JAXB_HOME%\lib\jaxb-libs.jar;%JAXB_HOME%\lib\jaxb-xjc.jar


"%JAVA_HOME%\bin\javac" -classpath %CLASSPATH%  %JAXR_HOME%\samples\jaxr-query\BusinessQueryTest.java

"%JAVA_HOME%\bin\java.exe" -classpath %CLASSPATH%;. -Dorg.apache.commons.logging.log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.defaultlog=warn BusinessQueryTest 


