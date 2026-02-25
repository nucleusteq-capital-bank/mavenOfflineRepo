Steps to build project
----------------------------
1. Generate gradle wrapper
   gradle wrapper
2. Build Project
   gradlew clean install

Steps for offline build
-------------------------
./gradlew clean build --refresh-dependencies

rsync -av ~/.gradle/caches/modules-2/files-2.1/ offline-repo/

./gradlew clean build --offline
