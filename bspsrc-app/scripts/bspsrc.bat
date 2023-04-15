@echo off
if "%~1" == "" (
	start <java_path>javaw -jar "%~dp0\bspsrc.jar" %*
) else (
	<java_path>java -jar "%~dp0\bspsrc.jar" %*
)