# Team 1 – Jira Analysis Plugins

## Developers

- Joonas Palm (Team Lead - 27.04.20 to 24.05.20) (joonas.palm@tum.de)

- Katharina Stetter (Team Lead - 24.05.20 to 21.06.20) (ga26jal@mytum.de)

- Valentin Bootz (v.bootz@tum.de)

- Ali Gharaee (ali.gharaee@tum.de)

## Getting started

Before you begin, you'll need [Oracle Java SE Development Kit 8 (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)   installed on your system

Then follow the tutorials to set up Atlassian SDK on [Linux and Mac](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-linux-or-mac-system/)  or [Windows](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-windows-system/) systems

After you set up the Atlassian SDK, the following steps will start up Jira with the plugin installed

 1. Change to the dashboard-plugin directory via terminal
 2. Use the 'atlas-run' command to install the plugin and run Jira on localhost
 3. Navigate to http://localhost:2990/jira via your browser
 4. As login credentials, use 'admin' for Username and Password

You have successfully installed the Atlassian Plugin!

Here are SDK commands you'll find useful:

* atlas-run   -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-help  -- prints description for all commands in the SDK