ChromeCast Java API v2
======================

At the moment I have started implementing this library, there was a java [implementation of V1 Google ChromeCast protocol](https://github.com/entertailion/Caster), which seems to be deprecated and does not work for newly created applications. The new V2 protocol is implemented by tools that come with Cast SDK, which is available for Android, iOS and Chrome Extension as javascript. Also there is a third party [implementation of V2 in Node.js](https://github.com/vincentbernat/nodecastor). This project is a third party implementation of Google ChromeCast V2 protocol in java.

Install
-------

The only available option for now is to build library from sources:

1) Clone github repo

    $ git clone https://github.com/vitalidze/chromecast-java-api-v2.git

2) Change to the cloned repo folder and run `mvn install`

    $ cd chromecast-java-api-v2.git
    $ mvn install

3) Then it could be included into project's `pom.xml` from local repository:

```xml
<dependencies>
...
  <dependency>
    <groupId>su.litvak.chromecast</groupId>
    <artifactId>api-v2</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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
if (chromecast.isAppAvailable("APP_ID") && status.getRunningApp("APP_ID") == null) {
  Application app = chromecastlaunchApp("APP_ID");
}
// Disconnect from device
chromecast.disconnect();
```

Finally, stop device discovery:

```java
ChromeCasts.stopDiscovery();
```

This is it for now. I am focused on media playment, so sending URL to play on ChromeCast device is the next step of implementation.

Useful links
------------

* [Implementation of V1 protocol in Node.js](https://github.com/wearefractal/nodecast)
* [Implementation of V2 protocol in Node.js](https://github.com/vincentbernat/nodecastor)

License
-------

(Apache v2.0 license)

Copyright (c) 2014 Vitaly Litvak vitavaque@gmail.com
