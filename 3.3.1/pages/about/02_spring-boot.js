<doc-view>

<h2 id="_spring_boot">Spring Boot</h2>
<div class="section">
<p>The Spring Boot module of the Coherence Spring project builds upon the core Spring module and enhances it by providing
dedicated auto-configuration support for Spring Boot as well support for Spring Boot configuration properties,
and support for Spring Boot&#8217;s new ConfigData API to use Coherence as a source of configuration data.</p>

<p>Getting your first Coherence powered Spring Boot application off the ground could not be easier. Create a basic Spring
Boot app using <a id="" title="" target="_blank" href="https://start.spring.io/">start.spring.io</a>.</p>

<p>Add the Coherence Spring Boot Starter to your generated app&#8217;s <code>pom.xml</code>:</p>

<markup
lang="xml"
title="Maven"
>&lt;dependencies&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;com.oracle.coherence.spring&lt;/groupId&gt;
        &lt;artifactId&gt;coherence-spring-boot-starter&lt;/artifactId&gt;
        &lt;version&gt;3.3.1&lt;/version&gt;
    &lt;/dependency&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;com.oracle.coherence.ce&lt;/groupId&gt;
        &lt;artifactId&gt;coherence&lt;/artifactId&gt;
        &lt;version&gt;22.06.1&lt;/version&gt;
    &lt;/dependency&gt;
&lt;/dependencies&gt;</markup>

<div class="admonition note">
<p class="admonition-inline">Instead of Coherence CE you can also specify the commercial version of Coherence.</p>
</div>
<p>This will be enough to start up a default instance of Coherence with default settings. E.g., inject Coherence beans into
your Spring Controllers, Services and other components:</p>

<markup
lang="java"

>@CoherenceCache("myCacheName")
private NamedCache&lt;Long, String&gt; namedCache;</markup>

<p>In order to dive deeper, please see the <a id="" title="" target="_blank" href="refdocs/reference/html/quickstart.html">Quickstart Guide</a>, the
<a id="" title="" target="_blank" href="https://github.com/coherence-community/todo-list-example/tree/master/java/spring-server">To-do List Sample</a>, and also
refer to the <a id="" title="" target="_blank" href="refdocs/reference/html/index.html">reference guide</a>.</p>

</div>
</doc-view>