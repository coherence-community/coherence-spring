# springmvc-tomcat-spike [![Travis CI Build Status](https://travis-ci.org/halvards/springmvc-tomcat-spike.svg?branch=master)](https://travis-ci.org/halvards/springmvc-tomcat-spike) [![Snap CI Build Status](https://snap-ci.com/halvards/springmvc-tomcat-spike/branch/master/build_image)](https://snap-ci.com/halvards/springmvc-tomcat-spike/branch/master)

Spike to work out what's required to run a Spring MVC application using embedded Tomcat, where the application is configured only with code and annotations, no XML files.

After launch, you should be able to go to <http://localhost:8080/app/hello> and see "Hello World".

The application port can be controlled using the `PORT` environment variable.

Run the tests using `./gradlew clean test` or `mvn clean verify`.

Build an executable jar complete with all dependencies using `./gradlew clean onejar` or `mvn clean compile assembly:single`.
