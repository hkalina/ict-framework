Yes/No app
==========

cordova prepare
bower install
ionic config build
cordova platform add <android/ios>
ionic run <android/ios>

Troubleshooting:
  fatal error: 'Cordova/CDVViewController.h' file not found
    cordova platform update ios
  unable to install iso-deploy:
    sudo npm -g install ios-deploy --unsafe-perm=true

IPA obtaining
*************

cordova build --release ios
cd platforms/ios
/usr/bin/xcodebuild archive -project YesNoApp.xcodeproj -scheme YesNoApp -archivePath YesNoAppArchive
xcodebuild -exportArchive -exportFormat IPA -archivePath YesNoAppArchive.xcarchive -exportPath yesno.ipa -exportProvisioningProfile "iOS Team Provisioning Profile: com.ionicframework.yesnoapp429803"

APK obtaining
*************

cordova build --release android
cp platforms/android/build/outputs/apk/android-release-unsigned.apk ../YesNoApp.apk
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ../community.keystore ../YesNoApp.apk community

To install from APK:
$ANDROID_HOME/platform-tools/adb install YesNoApp.apk

