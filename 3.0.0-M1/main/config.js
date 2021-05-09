function createConfig() {
    return {
        home: "about/01_overview",
        release: "3.0.0-M1",
        releases: [
            "3.0.0-M1"
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
        navLogo: 'images/logo.png'
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
            component: loadPage('about-01_overview', '/about/01_overview', {})
        },
        {
            path: '/about/03_spring-core',
            meta: {
                h1: 'Core Spring Framework',
                title: 'Core Spring Framework',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-03_spring-core', '/about/03_spring-core', {})
        },
        {
            path: '/about/04_spring-boot',
            meta: {
                h1: 'Spring Boot',
                title: 'Spring Boot',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-04_spring-boot', '/about/04_spring-boot', {})
        },
        {
            path: '/about/05_spring-cache',
            meta: {
                h1: 'Spring Cache',
                title: 'Spring Cache',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-05_spring-cache', '/about/05_spring-cache', {})
        },
        {
            path: '/about/06_spring-session',
            meta: {
                h1: 'Spring Session',
                title: 'Spring Session',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-06_spring-session', '/about/06_spring-session', {})
        },
        {
            path: '/about/07_spring-config-server',
            meta: {
                h1: 'Spring Config Server',
                title: 'Spring Config Server',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-07_spring-config-server', '/about/07_spring-config-server', {})
        },
        {
            path: '/about/08_spring-data',
            meta: {
                h1: 'Spring Data',
                title: 'Spring Data',
                h1Prefix: null,
                description: null,
                keywords: null,
                customLayout: null,
                hasNav: true
            },
            component: loadPage('about-08_spring-data', '/about/08_spring-data', {})
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
            component: loadPage('dev-01_license', '/dev/01_license', {})
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
            component: loadPage('dev-02_source-code', '/dev/02_source-code', {})
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
            component: loadPage('dev-03_build-instructions', '/dev/03_build-instructions', {})
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
            component: loadPage('dev-04_issue-tracking', '/dev/04_issue-tracking', {})
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
            component: loadPage('dev-05_contributions', '/dev/05_contributions', {})
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
            component: loadPage('dev-06_history', '/dev/06_history', {})
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
            component: loadPage('dev-07_getting-help', '/dev/07_getting-help', {})
        },
        {
            path: '/', redirect: '/about/01_overview'
        },
        {
            path: '*', redirect: '/'
        }
    ];
}

function createNav(){
    return [
        { header: 'Project Website' },
        {
            title: 'Components',
            action: 'assistant',
            group: '/about',
            items: [
                { href: '/about/01_overview', title: 'Overview' },
                { href: '/about/03_spring-core', title: 'Core Spring Framework' },
                { href: '/about/04_spring-boot', title: 'Spring Boot' },
                { href: '/about/05_spring-cache', title: 'Spring Cache' },
                { href: '/about/06_spring-session', title: 'Spring Session' },
                { href: '/about/07_spring-config-server', title: 'Spring Config Server' },
                { href: '/about/08_spring-data', title: 'Spring Data' }
            ]
        },
        {
            title: 'Development',
            action: 'fa-code',
            group: '/dev',
            items: [
                { href: '/dev/01_license', title: 'License' },
                { href: '/dev/02_source-code', title: 'Source Code' },
                { href: '/dev/03_build-instructions', title: 'Building' },
                { href: '/dev/04_issue-tracking', title: 'Issue Tracking' },
                { href: '/dev/05_contributions', title: 'Contributing' },
                { href: '/dev/06_history', title: 'Change History' },
                { href: '/dev/07_getting-help', title: 'Getting Help' }
            ]
        },
        { divider: true },
        { header: 'Reference Documentation' },
        {
            title: 'HTML',
            action: 'fa-html5',
            href: 'refdocs/reference/html/index.html',
            target: '_blank'
        },
        {
            title: 'Single Page',
            action: 'fa-html5',
            href: 'refdocs/reference/htmlsingle/index.html',
            target: '_blank'
        },
        {
            title: 'PDF',
            action: 'picture_as_pdf',
            href: 'refdocs/reference/pdf/coherence-spring-reference.pdf',
            target: '_blank'
        },
        {
            title: 'Javadocs',
            action: 'code',
            href: 'refdocs/api/index.html',
            target: '_blank'
        },
        { divider: true },
        { header: 'Additional Resources' },
        {
            title: 'Slack',
            action: 'fa-slack',
            href: 'https://join.slack.com/t/oraclecoherence/shared_invite/enQtNzcxNTQwMTAzNjE4LTJkZWI5ZDkzNGEzOTllZDgwZDU3NGM2YjY5YWYwMzM3ODdkNTU2NmNmNDFhOWIxMDZlNjg2MzE3NmMxZWMxMWE',
            target: '_blank'
        },
        {
            title: 'Coherence Web Site',
            action: 'fa-globe',
            href: 'https://coherence.community/',
            target: '_blank'
        },
        {
            title: 'Coherence Hibernate',
            action: 'fa-globe',
            href: 'https://hibernate.coherence.community/',
            target: '_blank'
        },
        {
            title: 'GitHub',
            action: 'fa-github-square',
            href: 'https://github.com/coherence-community/coherence-spring/',
            target: '_blank'
        },
        {
            title: 'Twitter',
            action: 'fa-twitter-square',
            href: 'https://twitter.com/OracleCoherence/',
            target: '_blank'
        }
    ];
}