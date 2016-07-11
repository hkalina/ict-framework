Yes/No app
==========

git submodule init
git submodule update
cordova prepare
bower install
ionic config build
cordova platform add <android/ios>
ionic run <android/ios>

Troubleshooting:
  general:
    cordova platform rm android
    cordova platform add android
  fatal error: 'Cordova/CDVViewController.h' file not found
    cordova platform update ios
  unable to install iso-deploy:
    sudo npm -g install ios-deploy --unsafe-perm=true
  Module 'ionic.service.core' is not available!
    ionic config build
  Code Sign error: No matching provisioning profiles found
    Open generated project using XCode: platforms/ios/YesNoApp.xcodeproj
    Change Bundle identifier to something own (not already registered)
    Press “Fix issue” after “No matching provisioning profile found”
    Deploy using “Play” button

IPA obtaining
*************

cordova build --release ios
cd platforms/ios
/usr/bin/xcodebuild archive -project YesNoApp.xcodeproj -scheme YesNoApp -archivePath YesNoAppArchive
xcodebuild -exportArchive -exportFormat IPA -archivePath YesNoAppArchive.xcarchive -exportPath yesno.ipa -exportProvisioningProfile "iOS Team Provisioning Profile: *"

APK obtaining
*************

cordova build --release android
cp platforms/android/build/outputs/apk/android-armv7-release-unsigned.apk ../YesNoApp-armv7.apk
cp platforms/android/build/outputs/apk/android-x86-release-unsigned.apk ../YesNoApp-x86.apk
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ../community.keystore ../YesNoApp-armv7.apk community
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ../community.keystore ../YesNoApp-x86.apk community

To install from APK:
$ANDROID_HOME/platform-tools/adb install YesNoApp.apk

