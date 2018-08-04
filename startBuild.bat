@echo config project
call configProject.bat %1
@echo config project success
@echo start clean
call clean.bat %1
@echo clean success
@echo start build
call build.bat %1
@echo build success
@echo copy apk
call copyFile.bat %1
@echo copy apk success
@echo start clean
call clean.bat %1
@echo clean success
@echo restore project
call restoreProject.bat %1
@echo restore project success

