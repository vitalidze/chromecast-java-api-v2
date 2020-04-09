ChromeCast Java API v2 [![Build Status](https://travis-ci.org/vitalidze/chromecast-java-api-v2.svg?branch=master)](https://travis-ci.org/vitalidze/chromecast-java-api-v2)
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
    <version>0.11.3</version>
  </dependency>
...
</dependencies>
```

Or to `build.gradle` (`mavenCentral()` repository should be included in appropriate block):

```groovy
dependencies {
// ...
    compile 'su.litvak.chromecast:api-v2:0.11.3'
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
    <version>0.11.4-SNAPSHOT</version>
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
// Connect (optional) 
// Needed only when 'autoReconnect' is 'false'. 
// Usually not needed and connection will be established automatically.
// chromecast.connect();
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
                null // media content type (optional, will be discovered automatically)
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

Since `v.0.9.0` there is a possibility to send custom requests using `send()` methods. It is required to implement at least `Request` interface for an objects being sent to the running application. If some response is expected then `Response` interface must be implemented. For example to send request to the [DashCast](https://github.com/stestagg/dashcast) application:

`Request` interface implementation

````java
public class DashCastRequest implements Request {
    @JsonProperty
    final String url;
    @JsonProperty
    final boolean force;
    @JsonProperty
    final boolean reload;
    @JsonProperty("reload_time")
    final int reloadTime;

    private Long requestId;

    public DashCastRequest(String url,
                           boolean force,
                           boolean reload,
                           int reloadTime) {
        this.url = url;
        this.force = force;
        this.reload = reload;
        this.reloadTime = reloadTime;
    }

    @Override
    public Long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
}
````

Sending request

````java
chromecast.send("urn:x-cast:es.offd.dashcast", new DashCastRequest("http://yandex.ru", true, false, 0));
````

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
* [Most recent .proto file for CastV2 protocol](https://github.com/chromium/chromium/blob/master/components/cast_channel/proto/cast_channel.proto)

Projects using library
----------------------

* [UniversalMediaServer](https://github.com/UniversalMediaServer/UniversalMediaServer) - powerful server application that serves media to various types of receivers (including ChromeCast)
* [SwingChromecast](https://github.com/DylanMeeus/SwingChromecast) - A graphical user interface to interact with your chromecasts. (Written in Java 8 with swing)


License
-------

(Apache v2.0 license)

Copyright (c) 2014 Vitaly Litvak vitavaque@gmail.com
