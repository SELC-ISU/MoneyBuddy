# MoneyBuddy
SE186X project from EZ Money, a local SQL-backed financing program letting you manage your checkbooks on the computer instead of paper! MoneyBuddy is SQLite backed and written purely in Java, allowing for portability and compatability.

# Installation
You can either clone this repository and compile the code yourself (not recommended), or you can just download the precompiled .jar file (recommended) from the [latest release](https://github.com/SELC-ISU/MoneyBuddy/releases)

# How-to
This program was designed to work straight out of the box, handling all the backend processes that come with database management. Assuming you want to keep track of a checkbook for just 1 account, you can start by entering your current balance into the "Amount" field and hitting submit. This will initialize your checkbook. Once you've done that, start entering negative values for expenses and positive values for income, just as you would with a normal paper checkbook. You can add memos (not parsed) and, if it's an expense, classify the expense as either a need or a want.

# To do
 - GUI: add functionality to display database statistics with database.getStatistics()
