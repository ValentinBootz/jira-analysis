# Team 1 – Jira Analysis Plugins

This project consists of a Jira Plugin Application containing custom Dashboard Items for the analysis of the project
state on Jira.

## Getting started

Before you begin, you'll need [Oracle Java SE Development Kit 8 (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
installed on your system.

Then follow the tutorials to set up Atlassian SDK on [Linux and Mac](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-linux-or-mac-system/)  or [Windows](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-windows-system/)
systems.

Make sure the Maven home directory of your IDE is pointing to the one included in the Atlassian SDK.

See [Useful Tricks and Common Issues](https://iwiki.sse.in.tum.de/x/YQC) for more tips and solutions for common issues.

## Running Jira locally

After you set up the Atlassian SDK, the following steps will start up Jira with the plugin installed:

 1. Change to the root directory of the project (the directory with the `pom.xml` file)
 2. Use the `atlas-run` command to install the plugin and run Jira on localhost
 3. Navigate to http://localhost:2990/jira via your browser
 4. As login credentials, use `admin` for Username and Password

Here are SDK commands you'll find useful:

* atlas-run   -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-help  -- prints description for all commands in the SDK

### Live development with QuickReload

Dashboard Items will be automatically reloaded by the [QuickReload plugin](https://developer.atlassian.com/server/framework/atlassian-sdk/automatic-plugin-reinstallation-with-quickreload/).
Start the application once and after making changes to the item just use the command `mvn package` in a separate
terminal and reload the Browser page.

### Generating mock data for Jira

To generate mock data you can use the [Data Generator for Jira](https://marketplace.atlassian.com/apps/1210725/data-generator-for-jira) plugin.
More details about the installation and usage can be found on [Confluence](https://iwiki.sse.in.tum.de/x/XACP).

### Cleaning mock data

Use the command `atlas-clean` to remove the target folder (be aware that all Jira data will be lost).

### Adjusting log levels

To adjust the log level of the Jira plugin:
 1. Navigate to `Administration -> System -> Logging and profiling`
 2. Scroll down and click `Configure logging level for another package`
 3. In the dialog insert `org.pit.jira` as the `Package name` and select the desired log level
 4. Click `Add`

## Installing Dashboard Items

The documentation for all implemented Dashboard Items can be found in [Confluence](https://iwiki.sse.in.tum.de/x/dwCP).

 1. After logging in to Jira, create or open a project
 2. At the top left of the page, click `Dashboard > Manage dashboards`
 3. Create or open a dashboard and click `Add gadget` button
 4. Select and add your dashboard item

## Developers

* Joonas Palm (Team Lead - 27.04.20 to 24.05.20) (joonas.palm@tum.de)
* Katharina Stetter (Team Lead - 24.05.20 to 21.06.20) (ga26jal@mytum.de)
* Valentin Bootz (v.bootz@tum.de)
* Ali Gharaee (ali.gharaee@tum.de)