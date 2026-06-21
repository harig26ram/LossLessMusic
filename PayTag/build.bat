@echo off
set JAVA_HOME=C:/Program Files/Java/jdk-17
set ANDROID_HOME=C:/Users/harig/android-sdk
cd /d "C:\Users\harig\OneDrive\Documents\Test API\code check\PayTag"
"%JAVA_HOME%\bin\java.exe" -classpath "gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain assembleRelease > build.log 2>&1
