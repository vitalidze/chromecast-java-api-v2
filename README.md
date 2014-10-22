ChromeCast Java API v2
======================

At the moment I have started implementing this library, there was a java [implementation of V1 Google ChromeCast protocol](https://github.com/entertailion/Caster), which seems to be deprecated and does not work for newly created applications. The new V2 protocol is implemented by tools that come with Cast SDK, which is available for Android, iOS and Chrome Extension as javascript. Also there is a third party [implementation of V2 in Node.js](https://github.com/vincentbernat/nodecastor). This project is a third party implementation of Google ChromeCast V2 protocol in java.

Install
-------

Library is available in maven central. Put lines below into you project's `pom.xml` file:

```xml
<dependencies>
...
  <dependency>
    <groupId>su.litvak.chromecast</groupId>
    <artifactId>api-v2</artifactId>
    <version>0.0.3</version>
  </dependency>
...
</dependencies>
```

Or to `build.gradle` (`mavenCentral()` repository should be included in appropriate block):

```groovy
dependencies {
// ...
    runtime 'su.litvak.chromecast:api-v2:0.0.3'
// ...
}
```

Build
-----

To build library from sources:

1) Clone github repo

    $ git clone https://github.com/vitalidze/chromecast-java-api-v2.git

2) Change to the cloned repo folder and run `mvn install`

    $ cd chromecast-java-api-v2
    $ mvn install

3) Then it could be included into project's `pom.xml` from local repository:

```xml
<dependencies>
...
  <dependency>
    <groupId>su.litvak.chromecast</groupId>
    <artifactId>api-v2</artifactId>
    <version>0.0.4-SNAPSHOT</version>
  </dependency>
...
</dependencies>
```

Usage
-----

This is still a work in progress. The API is not stable, the quality is pretty low and there are a lot of bugs.

To use the library, you first need to discover what Chromecast devices are available on the network.

```java
ChromeCasts.startDiscovery();
```

Then wait until some device discovered and it will be available in list. Then device should be connected. After that one can invoke several available operations, like check device status, application availability and launch application:

```java
ChromeCast chromecast = ChromeCasts.get().get(0);
// Connect
chromecast.connect();
// Get device status
Status status = chromecast.getStatus();
// Run application if it's not already running
if (chromecast.isAppAvailable("APP_ID") && !status.isAppRunning("APP_ID")) {
  Application app = chromecast.launchApp("APP_ID");
}
```

To start playing media in currently running media receiver:

```java
// play media URL directly
chromecast.load("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
// play media URL with additional parameters, such as media title and thumbnail image
chromecast.load("Big Buck Bunny",           // Media title
                "images/BigBuckBunny.jpg",  // URL to thumbnail based on media URL
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", // media URL
                "video/mp4" // media content type
                );
```

Then playback may be controlled with following methods:

```java
// pause playback
chromecast.pause();
// continue playback
chromecast.play();
// rewind (move to specified position (in seconds)
chromecast.seek(120);
// update volume
chromecast.setVolume(0.5f);
// mute
chromecast.setMuted(true);
// unmute (will set up volume to value before muting)
chromecast.setMuted(false);
```

Also there are utility methods to get current chromecast status (running app, etc.) and currently played media status:

```java
Status status = chromecast.getStatus();
MediaStatus mediaStatus = chromecast.getMediaStatus();
```

Current running application may be stopped by calling `stopApp()` method without arguments:

```java
// Stop currently running application
chromecast.stopApp();
```

Don't forget to close connection to ChromeCast device by calling `disconnect()`:

```java
// Disconnect from device
chromecast.disconnect();
```

Finally, stop device discovery:

```java
ChromeCasts.stopDiscovery();
```

Alternatively, ChromeCast device object may be created without discovery if address of chromecast device is known:

```java
ChromeCast chromecast = new ChromeCast("192.168.10.36");
```

This is it for now. It covers all my needs, but if someone is interested in more methods, I am open to make improvements.

Useful links
------------

* [Implementation of V1 protocol in Node.js](https://github.com/wearefractal/nodecast)
* [Console application implementing V1 protocol in java](https://github.com/entertailion/Caster)
* [GUI application in java using V1 protocol to send media from local machine to ChromeCast](https://github.com/entertailion/Fling)
* [Implementation of V2 protocol in Node.js](https://github.com/vincentbernat/nodecastor)
* [CastV2 protocol description](https://github.com/thibauts/node-castv2#protocol-description)
* [CastV2 media player implementation in Node.js](https://github.com/thibauts/node-castv2-client)
* [Library for Python 2 and 3 to communicate with the Google Chromecast](https://github.com/balloob/pychromecast)
* [CastV2 API protocol POC implementation in Python](https://github.com/minektur/chromecast-python-poc)

License
-------

(Apache v2.0 license)

Copyright (c) 2014 Vitaly Litvak vitavaque@gmail.com
