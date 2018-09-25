# Android Things End-to-End base sample (Kotlin)

This sample demonstrates integration between an Android Things device, Android companion
app, and the [Smart Home](https://developers.google.com/actions/smarthome/) APIs, using [Cloud Firestore](https://firebase.google.com/docs/firestore/) for the backend and [FCM](https://firebase.google.com/docs/cloud-messaging/) + [Cloud Functions](https://firebase.google.com/products/functions/)
for communication between the components.  It can be used as a starting point for building
your own Android projects with Assistant / Smarthome integration.

## Introduction

This sample allows control of an Android Things device with a Rainbow Hat mounted on top.  The user can:

- Tap buttons on the hat to turn LEDs on and off
- See the LED state from an Android phone (companion app)
- Control the LED state from the companion app
- Control the LED state from any Assistant device (phone, Google Home, etc)
  using smarthome apis -- e.g "Hey google, turn on the red LED on the rainbow hat."
- Report results back to the main UI thread once that work is complete

**IMPORTANT**: Please, note that these samples are not necessarily the easiest way to accomplish
a task. In particular, they handle all low level I/O protocols directly, on
purpose to showcase how to use the Peripheral APIs. In real world applications,
you should use or develop a suitable driver that encapsulates the manipulation
of low level APIs.

## Pre-requisites

- Android Things compatible board
- Android Studio 3.0+
- [Rainbow Hat for Android Things](https://shop.pimoroni.com/products/rainbow-hat-for-android-things)

## Build and install

There are three components to install, each in a subdirectory of this project.
- Android Things app : Follow instructions in [app module](./tree/master/app)
- Android companion app : Follow instructions in [companionApp module](./tree/master/companionApp)
- Firebase functions (Smarthome integration) : Follow instructions in
[Firebase module](./tree/master/firebase/functions)


## Enable auto-launch behavior

This sample app is currently configured to launch only when deployed from your
development machine. To enable the main activity to launch automatically on boot,
add the following `intent-filter` to the app's manifest file:

```xml
<activity ...>

    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.HOME"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>

</activity>
```
