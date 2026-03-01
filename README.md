Steps to build project
----------------------------
1. Generate gradle wrapper
   gradle wrapper
2. Build Project
   gradlew clean install

Steps for offline build
-------------------------
./gradlew clean bootstrapOfflineRepo \                   
  -PrepoMode=bootstrap \
  -PofflineRepoDir=offline-repo

  Verficiation
  ------------
  ./gradlew verifyOfflineRepo -PofflineRepoDir=offline-repo
