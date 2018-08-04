if exist "..\..\build\temp" (
rmdir /s/q "..\..\build\temp"
)

if not exist "..\..\build\temp" (
	mkdir "..\..\build\temp"
)

copy /y local.properties ..\..\build\temp

copy /y build.gradle ..\..\build\temp

if exist "local.properties" (
del /Q "local.properties"
)

copy /y ..\..\build\local.properties 

if exist "build.gradle" (
del /Q "build.gradle"
)

copy /y ..\..\build\build.gradle


if exist "..\..\build\libs\StorageManger.jar" (
del /Q "app\libs\StorageManger.jar"
)

if exist "..\..\build\libs\TPLog.jar" (
del /Q "app\libs\TPLog.jar"
)

if exist "..\..\build\libs\armeabi\libtplog.so" (
del /Q "app\libs\armeabi\libtplog.so"
)

if exist "..\..\build\libs\armeabi\libgnustl_shared.so" (
del /Q "app\libs\armeabi\libgnustl_shared.so"
)


if exist "..\..\build\libs\armeabi\libkdvlog.so" (
del /Q "app\libs\armeabi\libkdvlog.so"
)


if exist "..\..\build\libs\armeabi\libkprop.so" (
del /Q "app\libs\armeabi\libkprop.so"
)

if exist "..\..\build\libs\armeabi\libosp.so" (
del /Q "app\libs\armeabi\libosp.so"
)

if exist "..\..\..\10-common\lib\releaselib\release\android\libkdcrypto.so" (
del /Q "app\libs\armeabi\libkdcrypto.so"
)

if exist "..\..\..\10-common\lib\releaselib\release\android\libkdvmedianet.so" (
del /Q "app\libs\armeabi\libkdvmedianet.so"
)

if exist "..\..\..\10-common\lib\releaselib\release\android\libkdvsrtp.so" (
del /Q "app\libs\armeabi\libkdvsrtp.so"
)

if exist "..\..\..\10-common\lib\releaselib\release\android\libmediasdk.so" (
del /Q "app\libs\armeabi\libmediasdk.so"
)

if exist "..\..\..\10-common\lib\releaselib\release\android\mtapi\libmtcapidll-jni.so" (
del /Q "app\libs\armeabi\libmtcapidll-jni.so"
)

if exist "..\..\..\10-common\lib\releaselib\release\android\mtapi\libmtvncapidll.so" (
del /Q "app\libs\armeabi\libmtvncapidll.so"
)

if exist "..\..\..\10-common\lib\releaselib\release\android\libpfc.so" (
del /Q "app\libs\armeabi\libpfc.so"
)



if exist "..\..\..\10-common\lib\releaselib\release\android\mtapi\mtcapi-jni.jar" (
del /Q "app\libs\mtcapi-jni.jar"
)


if exist "..\..\..\10-common\lib\releaselib\release\android\jar\kdmediasdk.jar" (
del /Q "app\libs\kdmediasdk.jar"
)


copy /y ..\..\..\10-common\lib\releaselib\release\android\libkdcrypto.so app\libs\armeabi\
copy /y ..\..\..\10-common\lib\releaselib\release\android\libkdvmedianet.so app\libs\armeabi\
copy /y ..\..\..\10-common\lib\releaselib\release\android\libkdvsrtp.so app\libs\armeabi\
copy /y ..\..\..\10-common\lib\releaselib\release\android\libmediasdk.so app\libs\armeabi\
copy /y ..\..\..\10-common\lib\releaselib\release\android\mtapi\libmtcapidll-jni.so app\libs\armeabi\
copy /y ..\..\..\10-common\lib\releaselib\release\android\mtapi\libmtvncapidll.so app\libs\armeabi\
copy /y ..\..\..\10-common\lib\releaselib\release\android\libpfc.so app\libs\armeabi\
copy /y ..\..\..\10-common\lib\releaselib\release\android\mtapi\mtcapi-jni.jar app\libs\
copy /y ..\..\..\10-common\lib\releaselib\release\android\jar\kdmediasdk.jar app\libs\


copy /y ..\..\build\libs\StorageManger.jar app\libs
copy /y ..\..\build\libs\TPLog.jar app\libs\