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

  Windows
  --------
  gradle clean bootstrapOfflineRepo -PrepoMode=bootstrap -PofflineRepoDir=offline-repo

  Verficiation
  ------------
  ./gradlew verifyOfflineRepo -PofflineRepoDir=offline-repo
  
  gradle verifyOfflineRepo -PofflineRepoDir=offline-repo


Docker image
---------------

  docker build -t offline-gradle-repo:1.0.0 .

  Test if image is created : 
     docker images | grep offline-gradle-repo
  
