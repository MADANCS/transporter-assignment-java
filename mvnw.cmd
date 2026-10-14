@echo off
setlocal

set MAVEN_WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar

if not exist "%MAVEN_WRAPPER_JAR%" (
    echo Maven wrapper JAR not found at: %MAVEN_WRAPPER_JAR%
    exit /B 1
)

if defined JAVA_HOME (
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_CMD=java
)

"%JAVA_CMD%" -jar "%MAVEN_WRAPPER_JAR%" %*
endlocal
