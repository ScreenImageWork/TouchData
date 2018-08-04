if exist "local.properties" (
del /Q "local.properties"
)

copy /y ..\..\build\temp\local.properties 

if exist "build.gradle" (
del /Q "build.gradle"
)

copy /y ..\..\build\temp\build.gradle