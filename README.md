# README #

*Setup*

- git submodule init && git submodule update
- import into Eclipse google-play-services_lib
- import into Eclipse externals/cardslib/library/source/main as existing Android code
- in the cardslib (main) project, select java as source folder
- mark cardslib (main) as library
- import into Eclipse externals/google-maps-api-V2/library as existing Android code
- add to the google-maps-api-V2 build path <PATH_TO_YOUR_ANDROID_SDK>\android-sdk\extras\android\support\v4\android_support_v4.jar
- add google-play-services_lib as library for google-maps-api-V2
- mark google-maps-api-V2 (library) as library
- import into Eclipse SensibleJournal
