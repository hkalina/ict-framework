i-CT framework app
==================
1. open iCTFramework.jws using JDeveloper with installed MAF extension
2. Application - Deploy - iOS to iTunes
   - following error will occure: Code Sign error: No matching provisioning profiles found: No provisioning profiles with a valid signing identity (i.e. certificate and private key pair) were found.
3. Open generated project using Xcode: deploy/iOS1/temporary_xcode_project/Oracle_ADFmc_Container_Template.xcodeproj
4. Click on root of tree on left side
5. Set Bundle identifier to same your identifier (ict.framework for example)
6. Press Fix issue after “No provisioning profile found”
7. You can select your device in top menu and deploy using “Play” button
