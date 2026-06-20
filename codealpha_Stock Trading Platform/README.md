Project Structure
stock_trading_platform.py   # Main application (all classes + interactive menu)
portfolio_data.json          # Auto-generated file storing your saved portfolio
README.md                    # This file
Class Overview
Class	Responsibility
Stock	Represents a single stock (symbol, name, price) and simulates price movement
Market	Holds all available stocks, updates prices, displays market data
Transaction	Records a single buy/sell event
User	Manages cash balance, holdings, transactions, and portfolio value/history
TradingPlatform	Ties everything together via an interactive menu
How It Works
On startup, the platform loads your saved portfolio from portfolio_data.json if it exists — otherwise it creates a new user with $10,000 starting cash.
Each loop, stock prices are randomly adjusted (±3%) to simulate market activity, and your current portfolio value is recorded into your history.
You can buy/sell stocks, view live market prices, check your portfolio, and review past transactions — all from a simple text menu.
When you exit, your data is saved automatically so you can pick up where you left off.
Sample Menu
===== STOCK TRADING PLATFORM =====
1. View Market Data
2. Buy Stock
3. Sell Stock
4. View Portfolio
5. View Portfolio History
6. View Transaction History
7. Save & Exit
Getting Started
No command-line setup required beyond having Python 3 installed:

Download/clone this repository.
Open stock_trading_platform.py in your preferred IDE (VS Code, PyCharm, IDLE, etc.).
Run the file. You'll be prompted for a username on first run.
Use the menu options to explore the market, trade, and track your portfolio.
💡 Tip: Your data persists between runs via portfolio_data.json, located in the same folder as the script.

Possible Extensions
Replace the random price simulator with a real market data API (e.g., Alpha Vantage, Yahoo Finance).
Add a graphical interface (Tkinter, PyQt, or a web frontend with Flask/Django).
Support multiple users/accounts with login.
Add charts of portfolio value over time (e.g., using matplotlib).
Swap JSON storage for a real database (SQLite/PostgreSQL).
License
This project is free to use and modify for learning purposes.
