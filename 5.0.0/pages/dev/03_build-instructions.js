<doc-view>
<p>To build the Coherence Spring project you need to have the following software installed:</p>

<ul class="ulist">
<li>
<p><a target="_blank" href="https://www.oracle.com/java/technologies/downloads/">Java SE Development Kit 17 or 21</a>.</p>

</li>
<li>
<p><a target="_blank" href="http://maven.apache.org/">Apache Maven</a></p>

</li>
</ul>

<p>The default build uses Coherence CE 26.04 and targets Java 17 bytecode on either JDK.
The separate Coherence CE 26.10 test lane requires Java 21. See the
<a target="_blank" href="refdocs/reference/html/overview.html#compatibility">compatibility section</a> for runtime and test limits.</p>

<p>Once you have these installed building the entire source tree is as simple as executing the following shell command
(from the root of the source directory):</p>

<markup
lang="bash"

>./mvnw clean package</markup>

<p>For detailed instructions, please see the main
<a target="_blank" href="https://github.com/coherence-community/coherence-spring/blob/main/README.adoc">README</a> of the Coherence Spring source
code repository.</p>

</doc-view>
