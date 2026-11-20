<doc-view>

<h2 id="_change_history">Change History</h2>
<div class="section">
<p>The following sections outline Coherence Spring project changes in
reverse chronological order.</p>


<h3 id="_version_5_0_0">Version 5.0.0</h3>
<div class="section">
<p>Changes since <a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.3.1">Version 4.3.1</a>:</p>

<p><strong>Baseline and integration changes</strong></p>

<ul class="ulist">
<li>
<p>Moves to Spring Framework 7, Spring Boot 4, Spring Data 2025.1, Spring Cloud 2025.1,
Spring Session 4, Spring Security 7, and Jakarta EE 11. Samples use Jackson 3.
Java 17 remains the minimum. The default Coherence CE dependency is 26.04.</p>

</li>
<li>
<p>Adds <code>coherence-spring-session-boot-starter</code> for Boot servlet applications, including WAR deployments.
The general <code>coherence-spring-boot-starter</code> now makes <code>coherence-spring-session</code> and <code>spring-boot-session</code>
optional. Use the dedicated starter or declare all three integration artifacts explicitly.
Applications must also supply a Coherence runtime and servlet web dependencies.</p>

</li>
<li>
<p>Updates metrics integration for Boot 4. Add <code>coherence-micrometer</code> and provide a <code>MeterRegistry</code> bean.
Use <code>spring-boot-starter-actuator</code> or <code>spring-boot-starter-micrometer-metrics</code> to create a Boot-managed
registry, or supply your own. The general Coherence starter plus <code>coherence-micrometer</code> alone does
not create a registry or bind Coherence metrics.</p>

</li>
<li>
<p>Updates samples to <code>resilience4j-spring-boot4</code> and the Hibernate 7 adapter, <code>coherence-hibernate-cache-7</code>,
from Coherence Hibernate 4.1.0. Applications using that adapter must select a compatible Hibernate ORM
7.4.x version explicitly. Boot&#8217;s managed ORM version does not meet that requirement.</p>

</li>
</ul>

<p><strong>Fixes and behavior changes</strong></p>

<ul class="ulist">
<li>
<p>Fixes Boot Session timeout configuration for external WAR deployments. Set <code>spring.session.timeout</code>
for these deployments; <code>server.servlet.session.timeout</code> is not a fallback. Without a configured timeout,
the repository retains its 30-minute default. Embedded applications retain the server-property fallback.</p>

</li>
<li>
<p>Fixes POF serialization of <code>UsernamePasswordAuthenticationToken</code> with erased credentials or mixed
authority types. Preserves authentication state and adds a serializer for Spring Security 7 factor
authorities that preserves their issuance time.</p>

</li>
<li>
<p>Replaces the session-reset lambda with <code>SessionResetEntryProcessor</code>. Adds explicit serialization and
execution permissions for Session library operations on hardened Coherence runtimes.</p>

</li>
<li>
<p>Enables Java parameter-name metadata for Spring named-argument resolution.</p>

</li>
<li>
<p>Fixes Cloud Config sample JSON responses by copying lazy entity collections before response serialization.
Replaces the sample&#8217;s machine-specific Config Server Git path with <code>CONFIG_SERVER_GIT_URI</code>.</p>

</li>
<li>
<p>Fixes website resources and upgrade-guide links for sites served under version directories.</p>

</li>
</ul>

<p><strong>Known limitations and migration requirements</strong></p>

<ul class="ulist">
<li>
<p>POF session deployments need new registrations when they reset active sessions (<code>SessionResetEntryProcessor</code>)
or store Spring Security 7 factor authorities (<code>FactorGrantedAuthorityPofSerializer</code>).
Deploy the required classes and mappings to each relevant application and server endpoint.
Hardened runtimes also need application-owned permissions for session attributes, principals, and authentication details.</p>

</li>
<li>
<p>Mixed Coherence Spring 4.x/5.x access to a session cache and rollback after 5.x writes remain unqualified.
Automatic Session integration supports servlet applications; it does not provide reactive WebSession support.</p>

</li>
<li>
<p>The hardening CI lane selects released Coherence 26.10 on Java 21. This configuration does not establish
completed runtime qualification. Earlier snapshot results do not qualify the release artifacts.</p>

</li>
<li>
<p>Commercial Oracle Coherence 15.1.1.0.0 on Java 17/21 and 26.1.0.0.0 on Java 21 passed local qualification.
Coherence Spring 5.x supports commercial versions 15.1.1.0 and 26.1.0.0 on those JDKs; see the
<a target="_blank" href="refdocs/reference/html/overview.html#compatibility">compatibility section</a> for exact builds, expected skips and test limits.</p>

</li>
<li>
<p>Documents commercial support for the 4.x maintenance line: 15.1.1.0 on Java 17/21 and 26.1.0.0 on Java 21.
The 4.x/26.1.0.0 combination requires explicit <code>-Dcoherence.security.mode=compatibility</code> in every application,
client and server JVM before Coherence initializes; the unchanged 4.x integration graph fails with the hardened default.
See <a target="_blank" href="refdocs/reference/html/overview.html#compatibility-4">4.x commercial runtimes</a> for the tested revision and scope.</p>

</li>
</ul>

<p>See the <a target="_blank" href="refdocs/reference/html/upgrade-5.html">Coherence Spring 4.x to 5.x upgrade guide</a>
for dependency examples, POF configuration, session cut-over guidance, and application validation steps.</p>

</div>


<h3 id="_version_4_3_1_june_2025">Version 4.3.1 (June 2025)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.3.1" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.3.1</a></p>

</div>


<h3 id="_version_4_3_0_december_2024">Version 4.3.0 (December 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.3.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.3.0</a></p>

</div>


<h3 id="_version_4_2_0_september_2024">Version 4.2.0 (September 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.2.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.2.0</a></p>

</div>


<h3 id="_version_3_4_0_july_2024">Version 3.4.0 (July 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.4.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.4.0</a></p>

</div>


<h3 id="_version_4_1_3_march_2024">Version 4.1.3 (March 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.3" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.3</a></p>

</div>


<h3 id="_version_3_3_6_february_2024">Version 3.3.6 (February 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.6" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.6</a></p>

</div>


<h3 id="_version_4_1_2_february_2024">Version 4.1.2 (February 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.2" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.2</a></p>

</div>


<h3 id="_version_4_1_1_january_2024">Version 4.1.1 (January 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.1" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.1</a></p>

</div>


<h3 id="_version_3_3_5_january_2024">Version 3.3.5 (January 2024)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.5" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.5</a></p>

</div>


<h3 id="_version_4_1_0_november_2023">Version 4.1.0 (November 2023)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.1.0</a></p>

</div>


<h3 id="_version_3_3_4_november_2023">Version 3.3.4 (November 2023)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.4" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.4</a></p>

</div>


<h3 id="_version_4_0_0_july_2023">Version 4.0.0 (July 2023)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v4.0.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v4.0.0</a></p>

</div>


<h3 id="_version_3_3_3_july_2023">Version 3.3.3 (July 2023)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.3" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.3</a></p>

</div>


<h3 id="_version_3_3_2_november_2022">Version 3.3.2 (November 2022)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.2" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.2</a></p>

</div>


<h3 id="_version_3_3_1_september_2022">Version 3.3.1 (September 2022)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.1" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.1</a></p>

</div>


<h3 id="_version_3_3_0_july_2022">Version 3.3.0 (July 2022)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.3.0</a></p>

</div>


<h3 id="_version_3_2_0_april_2022">Version 3.2.0 (April 2022)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.2.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.2.0</a></p>

</div>


<h3 id="_version_3_1_0_january_2022">Version 3.1.0 (January 2022)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.1.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.1.0</a></p>

</div>


<h3 id="_version_3_0_0_september_201">Version 3.0.0 (September 201)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.0.0" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.0.0</a></p>

</div>


<h3 id="_version_3_0_0_m2_july_2021">Version 3.0.0.M2 (July 2021)</h3>
<div class="section">
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/releases/tag/v3.0.0-M2" class="bare">https://github.com/coherence-community/coherence-spring/releases/tag/v3.0.0-M2</a></p>

</div>


<h3 id="_version_3_0_0_m1_may_2021">Version 3.0.0.M1 (May 2021)</h3>
<div class="section">
<p><strong>Source and Documentation Contributors</strong></p>

<ul class="ulist">
<li>
<p>Gunnar Hillert</p>

</li>
<li>
<p>Ryan Lubke</p>

</li>
</ul>

<p><strong>Changes</strong></p>

<ul class="ulist">
<li>
<p>Change Licensing to UPL (<a target="_blank" href="https://www.apache.org/legal/resolved.html">Apache license compatible</a>)</p>

</li>
<li>
<p>Completely refactor bootstrapping mechanism using the new Coherence
<a target="_blank" href="https://coherence.community/21.06-M1/docs/#/docs/core/02_bootstrap">Bootstrap API</a> (Community Edition 20.12)</p>

</li>
<li>
<p>Add support for Spring Data repositories using the brand-new Coherence
<a target="_blank" href="https://coherence.community/21.06-M2/docs/#/docs/core/05_repository">Repository API</a> which has been introduced with
<a target="_blank" href="https://medium.com/oracle-coherence/coherence-ce-21-06-milestone-2-released-4467cc94f118">Coherence CE 21.06 M2</a></p>

</li>
<li>
<p>Add Spring Cache abstraction support</p>

</li>
<li>
<p>Add Spring Boot Starter</p>
<ul class="ulist">
<li>
<p>Provide AutoConfiguration support</p>

</li>
<li>
<p>Add initial support for Spring Boot configuration properties</p>

</li>
</ul>

</li>
<li>
<p>Add support for Coherence Event Listeners</p>

</li>
<li>
<p>Add initial support for Spring Boot <code>2.4+</code> Config API for importing additional configuration data using Coherence as a store
of configuration data</p>

</li>
<li>
<p>Add Dependency Injection (DI) support for various Coherence types:</p>
<ul class="ulist">
<li>
<p>Coherence Filters</p>

</li>
<li>
<p>Views</p>

</li>
<li>
<p>Coherence Sessions</p>

</li>
</ul>

</li>
<li>
<p>Inject Coherence Maps and Caches:</p>
<ul class="ulist">
<li>
<p>@CoherenceCache</p>

</li>
<li>
<p>@CoherenceMap</p>

</li>
<li>
<p>@CoherenceAsyncCache</p>

</li>
<li>
<p>@CoherenceAsyncMap</p>

</li>
</ul>

</li>
<li>
<p>Add support for Filter Binding Annotations</p>

</li>
<li>
<p>Add support for Extractor Binding Annotations</p>

</li>
<li>
<p>Add <a target="_blank" href="https://asciidoctor.org/docs/asciidoc-writers-guide/">AsciiDoc-based</a> documentation (HTML + PDF)</p>

</li>
<li>
<p>Convert the Coherence Spring website to the Coherence CE theme</p>

</li>
<li>
<p>Add security vulnerability scans using the <a target="_blank" href="https://github.com/jeremylong/DependencyCheck">OWASP Dependency-Check Plugin</a></p>

</li>
<li>
<p>Provide source-code consistency using the <a target="_blank" href="https://github.com/spring-io/spring-javaformat">Spring Java Format Maven Plugin</a></p>

</li>
<li>
<p>Add Quickstart Sample (Classic Spring + Spring Boot) and reference guide chapter</p>

</li>
<li>
<p>Add Spring Cloud Config demo</p>

</li>
<li>
<p>Add CI workflow using GitHub Actions</p>

</li>
<li>
<p>Add Coherence <a target="_blank" href="https://github.com/coherence-community/todo-list-example/tree/main/java/spring-server">To Do List Example Application</a>
for Spring Boot (separate repository)</p>

</li>
<li>
<p>Update to the latest dependencies</p>
<ul class="ulist">
<li>
<p>Oracle Coherence <code>21.06-M2</code></p>

</li>
<li>
<p>Spring Boot <code>2.4.5</code></p>

</li>
<li>
<p>Spring Framework <code>5.3.5</code></p>

</li>
</ul>

</li>
</ul>

</div>


<h3 id="_version_2_0_0_snapshot_built_on_2017_07_13_2003">Version 2.0.0-SNAPSHOT built on 2017-07-13 20:03</h3>
<div class="section">
<p><strong>Source and Documentation Contributors</strong></p>

<ul class="ulist">
<li>
<p>Brian Oliver</p>

</li>
</ul>

<p><strong>Global and Cross-Module Changes</strong></p>

<ul class="ulist">
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/8">Issue #8</a>: Ensured compliance with
Coherence 12.1.2 and 12.1.3 (for building and running) and 12.2.1 (for running)</p>

</li>
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/9">Issue #9</a>: Upgraded to use
Spring Framework 4.3.9</p>

</li>
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/10">Issue #10</a>: Upgraded to require Java 7</p>

</li>
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/9">Issue #11</a>: Made Spring Framework
dependencies scope "provided"</p>

</li>
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/13">Issue #13</a>: Introduced the
SpringBasedCoherenceSession to provide a mechanism to configure and acquire
Coherence-based resources (NamedCaches) from Coherence without using
static functions or methods.</p>

</li>
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/15">Issue #15</a>: Provided access to
Coherence Services via the SpringBasedCoherenceSession</p>

</li>
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/16">Issue #16</a>: Resolved incorrect XSD
declaration of spring:property</p>

</li>
<li>
<p><a target="_blank" href="https://github.com/coherence-community/coherence-spring/issues/17">Issue #17</a>: Migrated to use Maven Central
(from maven.java.net)</p>

</li>
</ul>

<v-divider class="my-5"/>
</div>


<h3 id="_version_1_0_2_built_on_2015_08_06_2045">Version 1.0.2 built on 2015-08-06 20:45</h3>
<div class="section">
<p><strong>Source and Documentation Contributors</strong></p>

<ul class="ulist">
<li>
<p>Brian Oliver</p>

</li>
</ul>

<p><strong>Global and Cross-Module Changes</strong></p>

<ul class="ulist">
<li>
<p><a target="_blank" href="https://java.net/jira/browse/COHSPR-8">COHSPR-8</a>: Upgraded to use
Coherence 12.1.2-0-4 (and certified against Coherence 12.1.3-0-2)</p>

</li>
</ul>

<v-divider class="my-5"/>
</div>


<h3 id="_version_1_0_1_built_on_2014_11_04_1109">Version: 1.0.1 built on 2014-11-04 11:09</h3>
<div class="section">
<p><strong>Acknowledgements</strong></p>

<p>Huge thanks to everyone that provided feedback, isolated defects and contributed fixes.</p>

<p><strong>Source and Documentation Contributors</strong></p>

<ul class="ulist">
<li>
<p>Brian Oliver</p>

</li>
</ul>

<p><strong>Global and Cross-Module Changes</strong></p>

<ul class="ulist">
<li>
<p>Upgraded Maven Plug-ins</p>

</li>
</ul>

<p><strong>The <code>coherence-spring</code> Module</strong></p>

<ul class="ulist">
<li>
<p><a target="_blank" href="https://java.net/jira/browse/COHSPR-5">COHSPR-5</a>: Resolved issue where
multiple CoherenceApplicationContexts were created during concurrent
initialization.</p>

</li>
</ul>

<v-divider class="my-5"/>
</div>


<h3 id="_version_1_0_0_built_on_2013_09_10_1335">Version: 1.0.0 built on 2013-09-10 13:35</h3>
<div class="section">
<p><strong>Acknowledgements</strong></p>

<p>Huge thanks to everyone that contributed feedback, suggestions, documentation
and ideas for this initial release.</p>

<p><strong>Source and Documentation Contributors</strong></p>

<ul class="ulist">
<li>
<p>Brian Oliver and Patrick Peralta</p>

</li>
</ul>

<p><strong>Global and Cross-Module Changes</strong></p>

<ul class="ulist">
<li>
<p>Initial Release</p>

</li>
</ul>

<p><strong>The <code>coherence-spring</code> Module</strong></p>

<ul class="ulist">
<li>
<p>Initial Release.</p>

</li>
</ul>

<p><strong>The <code>coherence-spring-tests</code> Module</strong></p>

<ul class="ulist">
<li>
<p>Initial Release.</p>

</li>
</ul>

</div>

</div>

</doc-view>
