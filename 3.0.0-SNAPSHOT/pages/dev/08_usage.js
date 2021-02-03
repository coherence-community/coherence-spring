<doc-view>

<h2 id="_usage">Usage</h2>
<div class="section">
<p>The Oracle Coherence Cache Configuration file <code>coherence-cache-config.xml</code>
allows use of the <code>&lt;class-scheme&gt;</code> and <code>&lt;instance&gt;</code> xml elements as mechanisms
to specify custom implementations of Coherence interfaces, such as <code>CacheStore</code>
and <code>MapListener</code>.  Traditionally Oracle Coherence uses these elements to
guide the instantiation of developer provided interface implementations in
two ways;  It can</p>

<ul class="ulist">
<li>
<p>Create a new instance of a specified class</p>

</li>
<li>
<p>Invoke a user-provided factory method to return a specific instance</p>

</li>
</ul>
<p>For some applications it may be useful for Coherence to retrieve objects
configured in a <code>&lt;class-scheme&gt;</code> element or <code>&lt;instance&gt;</code> element from alternative
sources, say for example a Spring <code>BeanFactory</code> instance, instead of creating
its own instance.  This is especially true for cache servers configured with
<code>CacheStore</code> objects running in a standalone JVM, because these <code>CacheStore</code>
objects typically must be configured with data sources, connection pools, and so on.
Spring provides the ability to configure such data sources for plain Java objects,
without requiring Java EE.</p>


<h3 id="_the_spring_namespace_handler">The Spring Namespace Handler</h3>
<div class="section">
<p>The Coherence Spring Namespace Handler <code>com.oracle.coherence.spring.SpringNamespaceHandler</code>
is a custom extension to Coherence that allows you to configure Coherence to
reference existing Spring beans in a cache configuration file instead of creating
new instances at runtime.</p>

<p>The references to Spring Beans are made declaratively in XML files using the
new Spring namespace for Coherence.  The schema definitions
for the Coherence Spring namespace elements are
described in the <code>coherence-spring-config.xsd</code> file. You can find this file
in the <code>coherence-spring.jar</code> file.</p>

<p> http://);</p>

</div>

<h3 id="_declaring_the_spring_namespace_handler">Declaring the Spring Namespace Handler</h3>
<div class="section">
<p>To use the Coherence Spring namespace handler, you must declare it in a cache
configuration file and then specify a factory for the Spring beans.</p>

<p>Use the following Coherence Spring namespace handler declaration in the
<code>&lt;cache-config&gt;</code> element of a cache configuration file:</p>

<pre>xmlns:spring="class://com.oracle.coherence.spring.SpringNamespaceHandler"</pre>
<p>To ensure proper configuration within the cache configuration file, the XSD file
<code>coherence-spring-config.xsd</code> that corresponds with this namespace handler can
also be referenced, as illustrated below:</p>

<pre>&lt;cache-config
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://xmlns.oracle.com/coherence/coherence-cache-config"
    xmlns:spring="class://com.oracle.coherence.spring.SpringNamespaceHandler"
    xsi:schemaLocation="http://xmlns.oracle.com/coherence/coherence-cache-config coherence-cache-config.xsd
                        class://com.oracle.coherence.spring.SpringNamespaceHandler coherence-spring-config.xsd"&gt;</pre>
<p>If your cache configuration must specify an externally provided user class by using a
 <code>&lt;class-scheme&gt;</code> or <code>&lt;instance&gt;</code> element, then this namespace handler can provide a
 Spring bean from a <code>BeanFactory</code>. To specify a Spring bean, use the <code>bean</code> and
 <code>bean-name</code> elements under the <code>&lt;class-scheme&gt;</code> or <code>&lt;instance&gt;</code> element:</p>

<pre>&lt;class-scheme&gt;
    &lt;spring:bean&gt;
        &lt;spring:bean-name&gt;listener&lt;/spring:bean-name&gt;
    &lt;/spring:bean&gt;
&lt;/class-scheme&gt;</pre>
<p>You can also use the property attribute to inject properties (including cache
configuration macros) into a bean, as illustrated below:</p>

<pre>&lt;spring:bean&gt;
    &lt;spring:bean-name&gt;listener&lt;/spring:bean-name&gt;
    &lt;spring:property name="backingMapManagerContext"&gt;{manager-context}&lt;/spring:property&gt;
&lt;/spring:bean&gt;</pre>
</div>

<h3 id="_specifying_a_factory_for_spring_beans">Specifying a Factory for Spring Beans</h3>
<div class="section">
<p>You can specify the bean factory that provides the Spring beans in one of the following ways:</p>

<ul class="ulist">
<li>
<p>Specify an Application Context in the cache configuration file</p>

</li>
<li>
<p>Specify a Bean Factory as a resource</p>

</li>
</ul>

<p>Use the <code>application-context-uri</code> element to specify the location of the Spring
application context XML file. Like the cache configuration file, the application
context file will be loaded either by using the file system or the classpath.
The <code>application-context-uri</code> element also supports URLs as values.
In the following example, <code>application-context.xml</code> represents the
application context XML file:</p>

<pre>&lt;cache-config
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://xmlns.oracle.com/coherence/coherence-cache-config"
    xmlns:spring="class://com.oracle.coherence.spring. SpringNamespaceHandler"
    xsi:schemaLocation="http://xmlns.oracle.com/coherence/coherence-cache-config coherence-cache-config.xsd
                        class://com.oracle.coherence.spring.SpringNamespaceHandler coherence-spring-config.xsd"&gt;

&lt;spring:bean-factory&gt;
    &lt;spring:application-context-uri&gt;application-context.xml&lt;/spring:application-context-uri&gt;
&lt;/spring:bean-factory&gt;</pre>
<p>Additionally, a Spring bean factory can be manually registered as a resource by
using the <code>registerResource</code> method, as illustrated below. In the example,
<code>DEFAULT_FACTORY_NAME</code> refers to the default Spring BeanFactory class:</p>

<pre>ConfigurableCacheFactory factory = CacheFactory.getCacheFactoryBuilder()
        .getConfigurableCacheFactory(...);

factory.getResourceRegistry().registerResource(
        BeanFactory.class,                            // type
        factoryName,                                  // resource name
        factory,                                      // factory reference
        null);                                        // optional
                                                      // ResourceLifecycleObserver</pre>
<p>If you specify a resource name other than the fully-qualified-name of the
BeanFactory.class, then reference that name in the bean element.
Use the <code>factory-name</code> element for the name of the custom bean factory and
<code>bean-name</code> for the name of the bean.</p>

<p>In the example below, the name of the bean factory is "custom-factory" and the
bean name is "listener":</p>

<pre>&lt;spring:bean&gt;
    &lt;spring:factory-name&gt;custom-factory&lt;/spring:factory-name&gt;
    &lt;spring:bean-name&gt;listener&lt;/spring:bean-name&gt;
&lt;/spring:bean&gt;</pre>
</div>
</div>
</doc-view>
