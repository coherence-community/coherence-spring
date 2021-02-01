# Coherence Spring Demo

[![Java CI with Maven](https://github.com/ghillert/coherence-spring-demo/workflows/Java%20CI%20with%20Maven/badge.svg?branch=main)](https://github.com/ghillert/coherence-spring-demo/actions) [![License](http://img.shields.io/badge/license-UPL%201.0-blue.svg)](https://oss.oracle.com/licenses/upl/)
[![Chat with us on slack](https://img.shields.io/badge/Coherence-Join%20Slack-red)](https://join.slack.com/t/oraclecoherence/shared_invite/zt-9ufv220y-Leudk0o5ntgNV0xraa8DNw)
[![Twitter Follow](https://img.shields.io/twitter/follow/OracleCoherence?style=social)](https://twitter.com/OracleCoherence)

In this demo we are show-casing Coherence support for Spring
applications using the [Coherence Spring project](https://github.com/coherence-community/coherence-spring).

In the initial version we start by providing an example of using Coherence for the
Spring Caching abstraction.

We provide 2 versions:

- `coherence-spring-demo-classic` Provides a demo using Spring Framework without Spring Boot
- `coherence-spring-demo-boot` Provide a demo using Spring Boot

Both demos provide exactly the same functionality. All common code is in the
`coherence-spring-demo-core` module.

## How to Run

Check out the project using [Git](https://git-scm.com/):

```bash
git clone https://github.com/ghillert/coherence-spring-demo.git
```

Build the demo using [Maven](https://maven.apache.org/):

```bash
./mvnw clean package
```

**Run the Spring Boot Demo:**

```bash
java -jar coherence-spring-demo-boot/target/coherence-spring-demo-boot-1.0.0-SNAPSHOT.jar
```

**Run the Classic Spring Framework Demo:**

```bash
java -jar coherence-spring-demo-classic/target/coherence-spring-demo-classic-1.0.0-SNAPSHOT.jar
```

Once started, the embedded database is empty. Let's create an event with 2 people added to them.

```bash
curl --request POST 'http://localhost:8080/api/events?title=First%20Event&date=2020-11-30'
```

This call will create an event and since the underlying executed method
`DefaultEventService#createAndStoreEvent()` is annotated with with `@CachePut(cacheNames="events", key="#result.id")`
the saved `Event` is added to the cache named `events` and ultimately also returned
and printed to the console:

```json
{
  "id" : 1,
  "title" : "First Event",
  "date" : "2020-11-30T00:00:00.000+00:00"
}
```

We see that an `Event` with the id `1` was successfully created. Let's verify
that the "cache put" worked by looking at the chache's statistics:

```bash
curl --request GET 'http://localhost:8080/api/statistics/events'
```

In the console you should see some basic statistics being printed including `"totalPuts" : 1,`:

```json
{
  "averageMissMillis" : 0.0,
  "cachePrunesMillis" : 0,
  "averagePruneMillis" : 0.0,
  "totalGetsMillis" : 0,
  "averageGetMillis" : 0.0,
  "totalPutsMillis" : 11,
  "averagePutMillis" : 11.0,
  "cacheHitsMillis" : 0,
  "averageHitMillis" : 0.0,
  "cacheMissesMillis" : 0,
  "cacheHits" : 0,
  "cacheMisses" : 0,
  "hitProbability" : 0.0,
  "totalPuts" : 1,
  "totalGets" : 0,
  "cachePrunes" : 0
}
```

Next, lets retrieve the `Event` using id `1`:

```bash
curl --request GET 'http://localhost:8080/api/events/1'
```

The `Event` is returned. Did you notice? No SQL queries were executed as the value
was directly retrieved from the Cache. Let check the statistics again by executing:

```bash
curl --request GET 'http://localhost:8080/api/statistics/events'
```

We will see now how values are being returned from the cache by seeing increasing
_cacheHits_, e.g. `"cacheHits" : 1,`.

Let's evict our `Event` with id `1` from the cache named `events`:

```bash
curl --request DELETE 'http://localhost:8080/api/events/1'
```

If you now retrieve the event again using:

```bash
curl --request GET 'http://localhost:8080/api/events/1'
```

you will see an SQL query executed in the console, re-populating the cache.

Feel free to play along with the Rest API. We can for example add people:

```bash
curl --request POST 'http://localhost:8080/api/people?firstName=Conrad&lastName=Zuse&age=85'
curl --request POST 'http://localhost:8080/api/people?firstName=Alan&lastName=Turing&age=41'

Or assign people to events:

```bash
curl --request POST 'http://localhost:8080/api/people/2/add-to-event/1'
curl --request POST 'http://localhost:8080/api/people/3/add-to-event/1'
```

## More Details

The Spring Boot application was created via https://start.spring.io.

```bash
wget https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.4.0&packaging=jar&jvmVersion=11&groupId=com.oracle.coherence.spring&artifactId=spring-demo&name=spring-demo&description=Demo%20project%20for%20Coherence%20Spring&packageName=com.oracle.coherence.spring.demo&dependencies=data-jpa,web,hsql
```

We have added 2 domain objects to the application:

* Person
* Event

## Enable Caching using Coherence CE

### Add Required Dependencies

First, please add the respective dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.oracle.coherence.spring</groupId>
    <artifactId>coherence-spring-core</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

You will also need to add a specific version of Coherence, e.g.:

```xml
<dependency>
    <groupId>com.oracle.coherence.ce</groupId>
    <artifactId>coherence</artifactId>
    <version>20.06.1</version>
</dependency>
```

### Configure Coherence

The Coherence caches and mappings are defined in `spring-coherence-cache-config.xml`.

We also added an _operational override file_ `tangosol-coherence-override.xml` to
the classpath in order to change the destination of Coherence's logging to the
[Simple Logging Facade for Java](http://www.slf4j.org/) (slf4j). For more information
please see the respective
[Coherence reference documentation](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.0/develop-applications/understanding-configuration.html#GUID-8387E29A-EE03-4075-B4E7-D92779335965).

### Configure Spring

In order to configure Spring, we will set the following Hibernate properties:

```properties
hibernate.cache.use_second_level_cache=true
hibernate.cache.region.factory_class=com.oracle.coherence.hibernate.cache.CoherenceRegionFactory
hibernate.cache.use_query_cache=true
```
For our Spring Boot based application, we can set those properties in
`application.yml`:

```yaml
spring:
  jpa:
    hibernate:
        properties:
          hibernate.cache.use_second_level_cache: true
          hibernate.cache.region.factory_class: com.oracle.coherence.hibernate.cache.CoherenceRegionFactory
          hibernate.cache.use_query_cache: true
          com.oracle.coherence.hibernate.cache.cache_config_file_path: test-hibernate-second-level-cache-config.xml
```

### Configure Service Layer

By default, your entity/model/domain objects are not cached. In order to make them
cacheable, we will use several of the annotation provided by Spring's Cache Abstraction:

- @CachePut - Always executes the annotated method, caches the return value
- @Cacheable - Only executes the method if the requested object is NOT cached

## REST Layer

Furthermore, we have 3 REST controllers.

### EventController

- GET `/api/events` Gets a paginated list of events
- POST `/api/events?title=foo&data=2020-10-30` Create a single event

## PersonController

- GET `/api/people` Gets a paginated list of events
- POST `/api/people?firstname=Eric&lastname=Cartman&age=10` Create a single person
- POST `/api/people/{personId}/add-to-event/{eventId}` Add a person to an event

## StatisticsController

- GET `/api/statistics` Returns Coherence statistics

With caching you may see a response similar to the following:

```json
{
  "averageMissMillis" : 7.0,
  "cachePrunesMillis" : 0,
  "averagePruneMillis" : 0.0,
  "totalGetsMillis" : 7,
  "averageGetMillis" : 3.5,
  "totalPutsMillis" : 11,
  "averagePutMillis" : 11.0,
  "cacheHitsMillis" : 0,
  "averageHitMillis" : 0.0,
  "cacheMissesMillis" : 7,
  "cacheHits" : 1,
  "cacheMisses" : 1,
  "hitProbability" : 0.5,
  "totalPuts" : 1,
  "totalGets" : 2,
  "cachePrunes" : 0
}
```
