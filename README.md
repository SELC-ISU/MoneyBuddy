# MoneyBuddy
SE186X project from EZ Money, a local SQLite-backed financing program letting you manage your checkbooks on the computer instead of paper! MoneyBuddy is written purely in Java, allowing for portability and compatability, and all database entries are stored on your local machine in ~/.MoneyBuddy/

# Installation
You can either clone this repository and compile the code yourself (not recommended), or you can just download the precompiled .jar file (recommended) from the [latest release](https://github.com/SELC-ISU/MoneyBuddy/releases)

# How-to
This program was designed to work straight out of the box, handling all the backend processes that come with database management. Assuming you want to keep track of a checkbook for just 1 account, you can start by entering your current balance into the "Amount" field and hitting submit. This will initialize your checkbook. Once you've done that, start entering negative values for expenses and positive values for income, just as you would with a normal paper checkbook. You can add memos (not parsed) and, if it's an expense, classify the expense as either a need or a want.

# Screenshots

This is an image of a populated checkbook as it's expected to be used:<br>
![home](../assets/home.PNG?raw=true)

This is image shows the file menu as it exists in MoneyBuddy v1.0.2. You can generate statistics, delete entries from the current database, or manage your different checkbooks from here:<br>
![file](../assets/file.PNG?raw=true)

Here you can see an irresponsible user who spent way more than they earned in May of 2020:<br>
![stats](../assets/stats.PNG?raw=true)

This is what it looks like to have more than 1 checkbook registered. These can be created or destroyed with the two buttons at the top of the menu:<br>
![multiple-checkbooks](../assets/multiple-checkbooks.PNG?raw=true)
