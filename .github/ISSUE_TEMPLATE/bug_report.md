---
name: Bug report
about: Create a bug report to help us improve
title: ''
labels: bug
assignees: ''

---

**Describe your setup**
- Apiman version
  - Post contents of http://localhost:8080/apimanui/api-manager/about (redact any sensitive information). 
- Environment
  - Operating system (e.g. Linux, Docker, Windows)
  - Java version (e.g. 8, 11, 17)
  - Any custom JVM settings?
- Manager platform e.g. Wildfly, Tomcat)
  -  Component configuration (provide redacted `apiman.properties` or describe).
- Gateway platform (e.g. Vert.x, Wildfly, Tomcat, etc)
  -  Component configuration (provide your redacted `conf.json`, `apiman.properties`, or describe your setup).
- Any alterations to the standard setup (e.g. JVM configuration)

**Describe the bug**
A clear and concise description of what the bug is.

- ❗ If your issue is with the UI, please post the contents of the Javascript developer console. You can use https://gist.github.com or a code block.
- ❗ If your issue causes an exception, please post the exception text (**not just a screenshot**). You can use https://gist.github.com or a code block.

**To Reproduce**
Steps to reproduce the behaviour:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behaviour**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Additional context**
Add any other context about the problem here.
