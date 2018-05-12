@ECHO OFF

::----------------------------------------------------------------------
:: Nitrite Data Gate startup script.
::----------------------------------------------------------------------

:: ---------------------------------------------------------------------
:: Ensure DATAGATE_HOME points to the directory where the datagate is installed.
:: ---------------------------------------------------------------------
SET DATAGATE_BIN_DIR=%~dp0
SET DATAGATE_HOME=%DATAGATE_BIN_DIR%..
SET DATAGATE_LIB_DIR=%DATAGATE_HOME%\lib
SET DATAGATE_EXE=%DATAGATE_LIB_DIR%\nitrite-datagate.jar
SET DATAGATE_CONF=%DATAGATE_HOME%\conf\datagate.properties
SET DATAGATE_LOG_DIR=%DATAGATE_HOME%\logs

:: ---------------------------------------------------------------------
:: Locate a JDK installation directory which will be used to run the DATAGATE.
:: Try (in order): JDK_HOME, JAVA_HOME.
:: ---------------------------------------------------------------------

SET JDK=
SET BITS=

IF EXIST "%JDK_HOME%" SET JDK=%JDK_HOME%
IF NOT "%JDK%" == "" GOTO check

IF EXIST "%JAVA_HOME%" SET JDK=%JAVA_HOME%

:check
SET JAVA_EXE=%JDK%\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" SET JAVA_EXE=%JDK%\jre\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" (
  ECHO ERROR: cannot start DATAGATE.
  ECHO No JDK found. Please validate either JDK_HOME or JAVA_HOME points to valid JDK installation.
  ECHO
  EXIT /B
)

SET JRE=%JDK%
IF EXIST "%JRE%\jre" SET JRE=%JDK%\jre
IF EXIST "%JRE%\lib\amd64" SET BITS=64

:: ---------------------------------------------------------------------
:: Run the Server.
:: ---------------------------------------------------------------------
SET OLD_PATH=%PATH%
SET PATH=%DATAGATE_BIN_DIR%;%PATH%

SET JAVA_OPTS=

IF NOT EXIST "%DATAGATE_LOG_DIR%" MD "%DATAGATE_LOG_DIR%"

"%JAVA_EXE%" -jar "%DATAGATE_EXE%" --spring.config.location=file:///"%DATAGATE_CONF%" -Ddatagate.log.dir="%DATAGATE_LOG_DIR%"

SET PATH=%OLD_PATH%