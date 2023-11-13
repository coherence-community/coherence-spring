function createConfig() {
    return {
        home: "about/01_overview",
        release: "4.1.1-SNAPSHOT",
        releases: [
            "4.1.1-SNAPSHOT"
        ],
        pathColors: {
            "*": "blue-grey"
        },
        theme: {
            primary: '#1976D2',
            secondary: '#424242',
            accent: '#82B1FF',
            error: '#FF5252',
            info: '#2196F3',
            success: '#4CAF50',
            warning: '#FFC107'
        },
        navTitle: 'Oracle Coherence Spring',
        navIcon: null,
        navLogo: 'images/logo.svg'
    };
}

function createRoutes(){
    return [
        {
            path: '/about/01_overview',
            meta: {
                h1: 'Overview',
                title: 'Overview',
                h1Prefix: null,
                description: 'Oracle Coherence Spring Website',
                keywords: 'coherence, spring, java, documentation',
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-01_overview', 'about/01_overview', {})
        },
        {
            path: '/about/02_spring-boot',
            meta: {
                h1: 'Spring Boot',
                title: 'Spring Boot',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-02_spring-boot', 'about/02_spring-boot', {})
        },
        {
            path: '/dev/01_license',
            meta: {
                h1: 'License',
                title: 'License',
                h1Prefix: null,
                description: 'Oracle Coherence Spring License',
                keywords: 'coherence, spring, license',
                customLayout: null,
                hasNav: true
            },
            component: loadPage('dev-01_license', 'dev/01_license', {})
        },
        {
            path: '/dev/02_source-code',
            meta: {
                h1: 'Source Code',
                title: 'Source Code',
                h1Prefix: null,
                description: 'Oracle Coherence Spring Website',
                keywords: 'coherence, spring, java, documentation',
                customLayout: null,
                hasNav: true
            },
            component: loadPage('dev-02_source-code', 'dev/02_source-code', {})
        },
        {
            path: '/dev/03_build-instructions',
            meta: {
                h1: 'Building',
                title: 'Building',
                h1Prefix: null,
                description: 'Oracle Coherence Spring Website',
                keywords: 'coherence, spring, java, documentation',
                customLayout: null,
                hasNav: true
            },
            component: loadPage('dev-03_build-instructions', 'dev/03_build-instructions', {})
        },
        {
            path: '/dev/04_issue-tracking',
            meta: {
                h1: 'Issue Tracking',
                title: 'Issue Tracking',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('dev-04_issue-tracking', 'dev/04_issue-tracking', {})
        },
        {
            path: '/dev/05_contributions',
            meta: {
                h1: 'Contributing',
                title: 'Contributing',
                h1Prefix: null,
                description: 'Oracle Coherence Spring Website',
                keywords: 'coherence, spring, java, documentation',
                customLayout: null,
                hasNav: true
            },
            component: loadPage('dev-05_contributions', 'dev/05_contributions', {})
        },
        {
            path: '/dev/06_history',
            meta: {
                h1: 'Change History',
                title: 'Change History',
                h1Prefix: null,
                description: 'Oracle Coherence Spring Website',
                keywords: 'coherence, spring, java, documentation',
                customLayout: null,
                hasNav: true
            },
            component: loadPage('dev-06_history', 'dev/06_history', {})
        },
        {
            path: '/dev/07_getting-help',
            meta: {
                h1: 'Getting Help',
                title: 'Getting Help',
                h1Prefix: null,
                description: 'Oracle Coherence Spring Website',
                keywords: 'coherence, spring, java, documentation',
                customLayout: null,
                hasNav: true
            },
            component: loadPage('dev-07_getting-help', 'dev/07_getting-help', {})
        },
        {
            path: '/', redirect: 'about/01_overview'
        },
        {
            path: '*', redirect: '/'
        }
    ];
}
function createNav(){
    return [
        {
            type: 'groups',
            items: [
                {
                    type: 'group',
                    title: 'Project Website',
                    group: '/about',
                    items: [
                        {
                            type: 'menu',
                            title: 'Getting Started',
                            group: '/about',
                            items: [
                                {
                                    type: 'page',
                                    title: 'Overview',
                                    to: '/about/01_overview',
                                    action: null
                                },
                                {
                                    type: 'page',
                                    title: 'Spring Boot',
                                    to: '/about/02_spring-boot',
                                    action: null
                                }
                            ],
                            action: 'assistant'
                        },
                        {
                            type: 'menu',
                            title: 'Development',
                            group: '/dev',
                            items: [
                                {
                                    type: 'page',
                                    title: 'License',
                                    to: '/dev/01_license',
                                    action: null
                                },
                                {
                                    type: 'page',
                                    title: 'Source Code',
                                    to: '/dev/02_source-code',
                                    action: null
                                },
                                {
                                    type: 'page',
                                    title: 'Building',
                                    to: '/dev/03_build-instructions',
                                    action: null
                                },
                                {
                                    type: 'page',
                                    title: 'Issue Tracking',
                                    to: '/dev/04_issue-tracking',
                                    action: null
                                },
                                {
                                    type: 'page',
                                    title: 'Contributing',
                                    to: '/dev/05_contributions',
                                    action: null
                                },
                                {
                                    type: 'page',
                                    title: 'Change History',
                                    to: '/dev/06_history',
                                    action: null
                                },
                                {
                                    type: 'page',
                                    title: 'Getting Help',
                                    to: '/dev/07_getting-help',
                                    action: null
                                }
                            ],
                            action: 'fa-code'
                        }
                    ],
                    action: null
                },
                {
                    type: 'group',
                    title: 'Reference Documentation',
                    group: '/docs',
                    items: [
                        {
                            type: 'link',
                            title: 'HTML',
                            href: 'refdocs/reference/html/index.html',
                            target: '_blank',
                            action: 'fa-html5'
                        },
                        {
                            type: 'link',
                            title: 'Single Page',
                            href: 'refdocs/reference/htmlsingle/index.html',
                            target: '_blank',
                            action: 'fa-html5'
                        },
                        {
                            type: 'link',
                            title: 'PDF',
                            href: 'refdocs/reference/pdf/coherence-spring-reference.pdf',
                            target: '_blank',
                            action: 'picture_as_pdf'
                        },
                        {
                            type: 'link',
                            title: 'Javadocs',
                            href: 'refdocs/api/index.html',
                            target: '_blank',
                            action: 'code'
                        }
                    ],
                    action: null
                },
                {
                    type: 'group',
                    title: 'Additional Resources',
                    group: null,
                    items: [
                        {
                            type: 'link',
                            title: 'Slack',
                            href: 'https://join.slack.com/t/oraclecoherence/shared_invite/enQtNzcxNTQwMTAzNjE4LTJkZWI5ZDkzNGEzOTllZDgwZDU3NGM2YjY5YWYwMzM3ODdkNTU2NmNmNDFhOWIxMDZlNjg2MzE3NmMxZWMxMWE',
                            target: '_blank',
                            action: 'fa-slack'
                        },
                        {
                            type: 'link',
                            title: 'Coherence Web Site',
                            href: 'https://coherence.community/',
                            target: '_blank',
                            action: 'fa-globe'
                        },
                        {
                            type: 'link',
                            title: 'Coherence Hibernate',
                            href: 'https://hibernate.coherence.community/',
                            target: '_blank',
                            action: 'fa-globe'
                        },
                        {
                            type: 'link',
                            title: 'GitHub',
                            href: 'https://github.com/coherence-community/coherence-spring/',
                            target: '_blank',
                            action: 'fa-github-square'
                        },
                        {
                            type: 'link',
                            title: 'Twitter',
                            href: 'https://twitter.com/OracleCoherence/',
                            target: '_blank',
                            action: 'fa-twitter-square'
                        }
                    ],
                    action: null
                }
            ],
            action: null
        }
    ];
}