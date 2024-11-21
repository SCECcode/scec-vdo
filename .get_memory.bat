@echo off

:: Get total memory in bytes using WMIC
for /f "tokens=2 delims==" %%A in ('wmic computersystem get totalphysicalmemory /value') do set TotalMemory=%%A

:: Trim whitespace
set TotalMemory=%TotalMemory:~0,-1%

:: Truncate the last 9 digits to convert bytes to gigabytes
set TotalMemoryGB=%TotalMemory:~0,-9%

:: Display the total memory in gigabytes
echo %TotalMemoryGB%

