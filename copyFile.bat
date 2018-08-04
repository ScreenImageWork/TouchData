if exist "..\..\..\10-common\version\apk\TouchData" (
    rmdir /s/q "..\..\..\10-common\version\apk\TouchData"
)

if not exist "..\..\..\10-common\version\apk\TouchData" (
    mkdir "..\..\..\10-common\version\apk\TouchData"
)

if not exist "..\..\..\10-common\version\apk\TouchData\release" (
    mkdir "..\..\..\10-common\version\apk\TouchData\release"
)


copy /y app\build\outputs\apk\app-release.apk ..\..\..\10-common\version\apk\TouchData\release


rename ..\..\..\10-common\version\apk\TouchData\release\app-release.apk TouchData.apk