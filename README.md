# Team 1 – Jira Analysis Plugins

This project consists of a Jira Plugin Application containing custom Dashboard Items for the analysis of the project
state on Jira.

## Getting started

Before you begin, you'll need [Oracle Java SE Development Kit 8 (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
installed on your system.

Then follow the tutorials to set up Atlassian SDK on [Linux and Mac](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-linux-or-mac-system/)  or [Windows](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-windows-system/)
systems.

Make sure the Maven home directory of your IDE is pointing to the one included in the Atlassian SDK.

## Running Jira locally

After you set up the Atlassian SDK, the following steps will start up Jira with the plugin installed:

 1. Use the `atlas-run` command to install the plugin and run Jira on localhost
 2. Navigate to http://localhost:2990/jira via your browser
 3. As login credentials, use `admin` for Username and Password

Here are SDK commands you'll find useful:

* atlas-run   -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-help  -- prints description for all commands in the SDK

## Installing Dashboard Items / Gadgets

 1. After logging in to Jira, create or open a project
 2. At the top left of the page, click `Dashboard > Manage dashboards`
 3. Create or open a dashboard and click `Add gadget` button
 4. Select and add your dashboard item

## Analysis Features

The following dashboard items are part of this plugin. They provide analysis on Jira data and are implemented based on Inverse Transparency concepts.

### Leaderboard [in Progress]

The leaderboard displays a ranking of developers based on the amount of tasks they completed.

## Developers

* Joonas Palm (Team Lead - 27.04.20 to 24.05.20) (joonas.palm@tum.de)
* Katharina Stetter (Team Lead - 24.05.20 to 21.06.20) (ga26jal@mytum.de)
* Valentin Bootz (v.bootz@tum.de)
* Ali Gharaee (ali.gharaee@tum.de)