# OreoAPI

[![Maven Central](https://img.shields.io/maven-central/v/io.github.el211/oreoapi?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.el211%22%20AND%20a:%22oreoapi%22)
[![Java](https://img.shields.io/badge/Java-21%2B-blue.svg)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

OreoAPI is a lightweight, framework‑agnostic Java library providing clean abstractions for MongoDB, RabbitMQ, and cross‑instance synchronization (publish/subscribe). It is built for Minecraft networks, proxies, backend services, and any Java application that needs reliable structured messaging and optional persistence — shipped as a pure Java artifact on Maven Central.

- Group ID: `io.github.el211`  
- Artifact ID: `oreoapi`  
- Version: `1.0.0`

Quick links
- Maven Central: io.github.el211:oreoapi:1.0.0
- Java: 21+

---

Why OreoAPI?

Many projects scatter infrastructure code across the codebase:
- “connect to mongo here”
- “publish rabbit here”
- “subscribe in random places”
- “packet IDs stored in 12 files”

OreoAPI centralizes these concerns behind stable, testable interfaces so you can:
- depend on simple, stable service interfaces
- swap real services with safe no-op implementations
- keep publish/subscribe consistent across instances
- structure and version messages with a packet registry
- remain independent of Bukkit/Paper (pure Java)

Main features
- MongoDB abstraction (IMongoService) — optional persistence layer
- RabbitMQ abstraction (IRabbitService) — publish/subscribe and RPC-ready patterns
- Sync bus (ISyncBus) — high-level cross-instance pub/sub routing
- Packet registry + namespaces — structured, versionable message types
- No-op implementations — safe fallbacks when services are disabled
- Pure Java artifact distributed via Maven Central (no Bukkit/Paper classes)

Table of contents
- Installation
- Quick start
- Examples
  - No-op (local mode)
  - RabbitMQ + Sync
  - Mongo usage (abstracted)
- Key concepts & API overview
- Configuration
- Logging & Debugging
- Releasing
- Compatibility
- Contributing
- License
- Author

Installation

Maven
```xml
<dependency>
  <groupId>io.github.el211</groupId>
  <artifactId>oreoapi</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle (Groovy)
```groovy
implementation 'io.github.el211:oreoapi:1.0.0'
```

Gradle (Kotlin DSL)
```kotlin
implementation("io.github.el211:oreoapi:1.0.0")
```

Quick start

OreoAPI is intentionally small on surface area. You construct implementations for the interfaces you need (real or no-op) and pass them into the main API entrypoint.

Minimal "No-op" Example (pure local mode)
```java
import fr.oreostudios.oreoapi.OreoApi;
import fr.oreostudios.oreoapi.mongo.NoopMongoService;
import fr.oreostudios.oreoapi.rabbit.NoopRabbitService;
import fr.oreostudios.oreoapi.sync.NoopSyncBus;

public class Main {
    public static void main(String[] args) {
        var mongo = new NoopMongoService();
        var rabbit = new NoopRabbitService();
        var sync = new NoopSyncBus("server-1");

        OreoApi api = new OreoApi(mongo, rabbit, sync);
        api.start();

        // your app logic here...

        api.stop();
    }
}
```

RabbitMQ + Sync Example

Publish a message
```java
sync.publish(
    "oreo.broadcast",
    "ANNOUNCE",
    Map.of("message", "Hello network!", "from", sync.serverId())
);
```

Subscribe to a topic
```java
sync.subscribe("oreo.broadcast", (routingKey, type, payload) -> {
    System.out.println("Received: " + type + " -> " + payload);
});
```

Mongo Example (conceptual)

Because IMongoService abstracts the driver, your app code is stable and independent from changes to the underlying storage.

```java
mongo.connect();

// You would expose higher-level repository methods in your own code, e.g.
// mongo.users().insert(...)
// mongo.settings().find(...)

mongo.close();
```

Key concepts & API overview

- IMongoService
  - Abstracts MongoDB operations and lifecycle (connect/close).
  - Implementations: MongoService (real), NoopMongoService (disabled).
- IRabbitService
  - Abstracts RabbitMQ publish/subscribe fundamentals.
  - Implementations: RabbitService (real), NoopRabbitService (disabled).
- ISyncBus
  - High-level publish/subscribe bus with routing keys and server IDs.
  - Built on top of IRabbitService.
  - Implementations: SyncBus (real), NoopSyncBus (disabled).
- Packet Registry
  - Maps numeric/ID packet identifiers to typed classes.
  - Supports namespaces for versioning and isolation.
- No-op Implementations
  - Intentionally included to let you wire safely when external infra is disabled.
  - No-op publish typically throws (to surface configuration mistakes), while other no-op operations are safe.

Configuration

- Service enablement: decide at startup whether to instantiate real implementations or Noop versions (recommended pattern).
- RabbitMQ: supply host, credentials, virtual host, exchanges, and durable queue settings in your app’s configuration (not baked into OreoAPI).
- MongoDB: provide connection URI, database name, and optional codec/mapper as needed.

Logging & Debugging

- OreoAPI does not prescribe a logging framework. Use SLF4J / Logback / Log4J in your host application.
- Increase log level for networking and messaging libraries during debugging (client driver, Rabbit client).
- Noop implementations emit minimal logs; publishing while disabled will surface an explicit error where configured.

Releasing

OreoAPI is built and published to Maven Central and signed with GPG.

To release:
```bash
mvn -Prelease clean deploy
```

Compatibility

- Java 21+ recommended
- Works in:
  - Standalone Java applications
  - Minecraft proxies (Velocity, Bungee) when integrated in a wrapper module
  - Bukkit/Paper projects as a dependency (library-only; no server classes included)

Contributing

Contributions are welcome — bug reports, feature requests, and PRs. Please:
1. Open an issue to discuss large changes.
2. Follow the existing code style and include tests where appropriate.
3. Sign commits if required by your project rules (GPG for releases only).

Suggested project layout (recommended)
- oreoapi — core library (this artifact)
- oreoapi-bukkit — optional wrapper for Bukkit/Paper (separate artifact)
- oreoapi-app — example standalone app / proxy integration

FAQ

Q: Do users need to set extra repositories?
A: No — OreoAPI is published to Maven Central.

Q: Is OreoAPI a Bukkit plugin?
A: No. It is a library. If you need a Bukkit plugin, create a separate wrapper module.

Q: Why do Noop services throw on publish?
A: Publishing while the messaging layer is disabled is a configuration error. Throwing prevents silent message loss and helps surface problems early.

API Reference & Javadoc

Include a link to hosted Javadoc or GitHub Packages if you publish docs. If you want, I can generate a short "public API" section listing the primary interfaces and common method signatures.

License

OreoAPI is released under the MIT License. See LICENSE for details.

Author

Elias — Oreo Studios
Contact: (add email or GitHub handle)

Acknowledgements

Built for maintainable cross-instance messaging and persistence across Java services and Minecraft ecosystems.

Changelog

See CHANGELOG.md for release notes.

---


