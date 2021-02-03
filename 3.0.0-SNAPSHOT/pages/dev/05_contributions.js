<doc-view>

<h2 id="_contributing">Contributing</h2>
<div class="section">
<p>The following sections outline the requirements for making contributions to the
Coherence Spring project.</p>


<h3 id="_overview">Overview</h3>
<div class="section">
<p>Developing code and documentation for the Coherence Spring project is a
<strong>big deal</strong>.</p>

<p>While it may seemingly be a place to develop and possibly experiment with patterns
or solutions, the fact of the matter is that the Coherence Community frequently
adopts the styles, approaches and implementations provided by the
Coherence Spring , whether in total or in part, for use in production scenarios.</p>

<p>Consequently every effort has to be made to ensure that projects are somewhat
stable, not too experimental and offer a high-degree of reliability, scalability
and stability, something that people can trust if they decide to branch or
become inspired by said projects on their own.</p>

<p>Foremostly the Coherence Spring project is not designed to be a "holding zone"
for individually or corporately developed utilities based on Coherence.  While
these may be of interest to individuals, we carefully investigate utility of
these solutions before considering them for inclusion.  In most cases we
instead encourage developers to define their own "common" projects.</p>

</div>

<h3 id="_becoming_a_contributor">Becoming a Contributor</h3>
<div class="section">
<p>Contributing to the Coherence Spring project can be done in various ways, each
of which is valuable to the Coherence Community as a whole.  Contributions may
include helping out by answering questions on the forums, isolating and submitting
defects resolution requests, proposing or submitting defect resolutions (fixes),
suggesting or building enhancements, introducing new features and improving tests
and documentation.</p>

<p>For all contributions that involve making a change to the source tree and thus
releasing a new build of the Coherence Spring project, those primarily
being a defect fix, enhancement, new feature or documentation improvement, all
non-Oracle contributors must complete and sign the
<a id="" title="" target="_blank" href="https://www.oracle.com/technetwork/community/oca-486395.html">Oracle Contributors Agreement</a>.</p>

<p>To do this, simply print out the form, fill in the necessary details, scan it
in and return via email to: oracle-ca_us [at] oracle [dot] com.</p>

<p><strong>Note 1:</strong> For the "Project Name:" please write "Coherence".</p>

<p><strong>Note 2:</strong> This is the same agreement used for making contributions to GlassFish and Java
itself.  If you already have executed this agreement then you&#8217;re ready to
contribute to the Coherence Spring project.</p>

<p>Should you have any questions regarding this agreement, you should consult the
<a id="" title="" target="_blank" href="http://www.oracle.com/technetwork/oca-faq-405384.pdf">Oracle Contributors Agreement FAQ</a></p>

</div>

<h3 id="_roles_and_responsibilities">Roles and Responsibilities</h3>
<div class="section">
<p>As mentioned above, there are quite a few ways to participate on projects that
are part of the Coherence Spring project, and not all of them involve
contributing source code!  Simply using the software, participating on mailing
lists or forums, filing bug reports or enhancement requests are an incredibly
valuable form of participation.</p>

<p>If one were to break down the forms of participation for the Coherence Spring project
into a set of roles, the result would look something like this:</p>

<pre>Users, Contributors, Committers, Maintainers, and Project Leads.</pre>
<p><strong>Users:</strong></p>

<p>Users are the people who use the software. Users are using the software, reporting
bugs, making feature requests and suggestions. This is by far the most important
category of people. Without users, there is no reason for the project.</p>

<p><em>How to become one:</em> Download the software and use it to build an application.</p>

<p><strong>Contributors:</strong></p>

<p>Contributors are individuals who contribute to an Coherence Spring project,
but do not have write access to the source tree. Contributions can be in the form of
source code patches, new code, or bug reports, but could also include web site
content like articles, FAQs, or screenshots.</p>

<p>A contributor who has sent in solid, useful source code patches on a project
can be elevated to Committer status by a Maintainer.</p>

<p>Integration of a Contributor submissions done at the discretion of a Maintainer,
but this is an iterative, communicative process. Note that for code to be
integrated, a completed Oracle Contribution Agreement is required from
each contributor.</p>

<p><em>How to become one:</em> Contribute in any of the ways described above: either code,
examples, web site updates, tests, bugs, and patches. If you&#8217;re interested in
becoming a Committer to the source base, get the sources to the project, make
an improvement or fix a bug, and send that code to the developers mailing list
or attach it to the bug report in the project issue tracking system.</p>

<p><strong>Committers:</strong></p>

<p>Committers have write access to the source tree, either for the individual
modules they are working on, or in some cases global write permissions
everywhere in the source code management system.</p>

<p>A Committer must first become a Contributor before they can be granted access
to commit to the source tree.</p>

<p>Rules for how to commit, once you have commit access, will vary by project and
module. Be sure to ask before you start making changes!</p>

<p><em>How to become one:</em> Submit some patches via email, and ask the Maintainer of
the code you&#8217;ve patched for commit access. The Maintainer will seek consensus
before granting Committer access, but their decisions are final.</p>

<p><strong>Maintainers:</strong></p>

<p>Each module has one Maintainer, who has check-in permissions (either for that
module or globally), and "manages" a group of Committers. They are responsible
for merging contributed patches, bug fixes, and new code from the development
branch of the source tree into the stable branch. Maintainers are responsible
for making sure that these contributions do not break the build.</p>

<p>The Maintainer is also responsible for checking that everyone who contributes
code has submitted an Oracle Contribution Agreement.</p>

<p>A Maintainer is responsible for their module, and for granting check-in
privileges to Contributors. They also act as the "police force" of the module,
helping to ensure quality across the build.</p>

<p><em>How to become one:</em></p>

<ul class="ulist">
<li>
<p>Start a module (you need to have written some working code on your project
to do this, you&#8217;ll also need to talk to the Project Lead).</p>

</li>
<li>
<p>Have responsibility for that module handed over to you from the current Maintainer.</p>

</li>
<li>
<p>Take over an abandoned project&#8212;&#8203;sometimes someone starts something, but for
one reason or another can&#8217;t continue to work on it. If it&#8217;s interesting to you,
volunteer!</p>

</li>
</ul>
<p><strong>Project Lead:</strong></p>

<p>Each project in the Coherence Spring project has an overall Project Lead.
The Project Leads are currently appointed by Oracle. They are responsible for
managing the entire project, helping to create policies by consensus that
ensure global quality.</p>

</div>

<h3 id="_making_a_contribution">Making a Contribution</h3>
<div class="section">
<p>All contributors are required to be a Committer in order to commit
contributions to the Coherence Spring project, including documentation.</p>

<p>To become a Committer you must first clearly demonstrate both
skill as a developer and be capable of strictly adhering to the quality
and architectural requirements of the Coherence Spring project.   In order
to demonstrate these abilities, it&#8217;s best to get started by submitting patches
or improvements via email and then asking the Maintainer or Project Lead to
review said changes, after which they may be accepted and submitted (either by
the Maintainer or Project Lead).</p>

<p>Coherence Spring follows the Spring coding conventions.</p>

</div>
</div>
</doc-view>
