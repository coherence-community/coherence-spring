<doc-view>

<h2 id="_spring_boot">Spring Boot</h2>
<div class="section">
<p>The Spring Boot module of the Coherence Spring project builds upon the core Spring module and enhances it by providing
dedicated auto-configuration support for Spring Boot as well support for Spring Boot configuration properties,
and support for Spring Boot&#8217;s new ConfigData API to use Coherence as a source of configuration data.</p>

<p>Getting your first Coherence powered Spring Boot application off the ground could not be easier. Create a basic Spring
Boot app using <a target="_blank" href="https://start.spring.io/">start.spring.io</a>.</p>

<p>Add the Coherence Spring Boot Starter to your generated app&#8217;s <code>pom.xml</code>:</p>

<markup
lang="xml"
title="Maven"
>&lt;dependencies&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;com.oracle.coherence.spring&lt;/groupId&gt;
        &lt;artifactId&gt;coherence-spring-boot-starter&lt;/artifactId&gt;
        &lt;version&gt;5.0.0&lt;/version&gt;
    &lt;/dependency&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;com.oracle.coherence.ce&lt;/groupId&gt;
        &lt;artifactId&gt;coherence&lt;/artifactId&gt;
        &lt;version&gt;26.04&lt;/version&gt;
    &lt;/dependency&gt;
&lt;/dependencies&gt;</markup>

<p>Coherence Spring 5.0.0 uses Spring Framework 7 and Spring Boot 4. Its supported default is
Coherence CE 26.04 on Java 17 or Java 21. Coherence Spring targets Java 17 bytecode.
Coherence Spring 5.x supports commercial Oracle Coherence 15.1.1.0 on Java 17/21 and 26.1.0.0 on Java 21. See the
<a target="_blank" href="refdocs/reference/html/overview.html#compatibility">compatibility section</a> for exact builds, expected skips,
test limits and CE 26.10/Java 21 runtime requirements.</p>

<p>The 4.x maintenance line supports commercial 15.1.1.0 on Java 17/21 and 26.1.0.0 on Java 21. For 4.x with 26.1.0.0,
set <code>-Dcoherence.security.mode=compatibility</code> in every application, client and server JVM before Coherence initializes.
See <a target="_blank" href="refdocs/reference/html/overview.html#compatibility-4">4.x commercial runtimes</a> for the tested revision and limits.</p>

<p>If you are moving an existing application from Coherence Spring 4.x, follow the
<a target="_blank" href="refdocs/reference/html/upgrade-5.html">Coherence Spring 5.x upgrade guide</a> before changing dependencies.</p>

<p>This will be enough to start up a default instance of Coherence with default settings. E.g., inject Coherence beans into
your Spring Controllers, Services and other components:</p>

<markup
lang="java"

>@CoherenceCache("myCacheName")
private NamedCache&lt;Long, String&gt; namedCache;</markup>

<p>In order to dive deeper, please see the <a target="_blank" href="refdocs/reference/html/quickstart.html">Quickstart Guide</a>, the
<a target="_blank" href="https://github.com/coherence-community/todo-list-example/tree/main/java/spring-server">To-do List Sample</a>, and also
refer to the <a target="_blank" href="refdocs/reference/html/index.html">reference guide</a>.</p>

</div>

</doc-view>
