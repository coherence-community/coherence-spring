<doc-view>
<p>To build the Coherence Spring project you need to have the following software installed:</p>

<ol style="margin-left: 15px;">
<li>
Java SE Development Kit 11.
<p>Available from: <a id="" title="" target="_blank" href="http://www.oracle.com/technetwork/java/javase/downloads/index.html">Oracle Java SE Downloads</a></p>

</li>
<li>
Apache Maven Version 3.3.9
<p>Available here: <a id="" title="" target="_blank" href="http://maven.apache.org/">Apache Maven Downloads</a></p>

</li>
<li>
The <a id="" title="" target="_blank" href="source-code.html">source code</a>.

</li>
</ol>
<p>Once you have these installed building the entire source tree is as simple as
executing the following shell command (from the root of the source directory):</p>

<pre>$ mvn clean install</pre>
<p>However the fact that the Coherence Spring project depends on a commercial
product, namely <a id="" title="" target="_blank" href="http://www.oracle.com/technetwork/middleware/coherence/overview/index.html">Oracle Coherence</a> that of which is not available in a
public <a id="" title="" target="_blank" href="http://maven.apache.org/">Apache Maven</a> Repository, probably means that your first build is likely
to fail because of a missing dependency on <code>coherence.jar</code>.</p>

<p>Should this problem occur you may need to manually install a suitable
<code>coherence.jar</code> into your local repository or your organization&#8217;s repository
manager.</p>

<p>If you haven&#8217;t used or don&#8217;t plan on using the Maven support provided with
the Oracle Coherence installed, to manually install the standard <code>coherence.jar</code>
into your local Apache Maven repository, simply change to <code>$COHERENCE_HOME/lib</code>
directory and run the following command:</p>

<pre>$ mvn install:install-file  \
      -DgroupId=com.oracle.coherence  \
      -DartifactId=coherence  \
      -Dversion=${site-coherence-version}  \
      -Dfile=coherence.jar  \
      -Dpackaging=jar \
      -DgeneratePom=true</pre>

<p>Of course the above assumes that you are installing the Coherence
{site-coherence-version} <code>jar</code> file. If you want to install one of the more
recent releases, change the version number in the command above accordingly.</p>

<p>Once the <code>coherence.jar</code> is properly installed into your <a id="" title="" target="_blank" href="http://maven.apache.org/">Apache Maven</a>
repository, you should be able to build the Coherence Spring project.</p>

</doc-view>
