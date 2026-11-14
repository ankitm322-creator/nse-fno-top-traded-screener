# NSE F&O Top Traded Value Screener

A simple market screener for NSE F&O stocks ranked by traded value using the FYERS API. It renders a live list of major F&O names and auto-refreshes every minute.

## Features

- Top traded value screener for NSE F&O symbols
- FYERS live data integration
- Demo fallback mode when API credentials are missing
- Search/filter by symbol
- Refresh button and auto-refresh every 60 seconds
- Clean single-page dashboard

## Quick start

1. Install dependencies:

   npm install

2. Copy the environment file:

   cp .env.example .env

3. Add your FYERS credentials:

   FYERS_APP_ID=YOUR_APP_ID
   FYERS_ACCESS_TOKEN=YOUR_ACCESS_TOKEN

4. Start the app:

   npm start

5. Open:

   http://localhost:3000

## FYERS API notes

This app expects a valid FYERS app ID and access token. The app uses the FYERS market quotes endpoint and sorts results by traded value descending.

If the credentials are missing or the API call fails, the app automatically falls back to demo mock data so the dashboard keeps working.

## Project structure

- `server.js` – Express backend and FYERS integration
- `public/index.html` – dashboard shell
- `public/styles.css` – custom styling
- `public/app.js` – frontend rendering logic

## Important

- Never commit real credentials to GitHub.
- Keep your `.env` file outside version control.
- This is a sample screener for educational and app-planning use; always validate with your own FYERS account and risk rules.

## Example live usage

Try using a watchlist of major stock symbols such as:

- RELIANCE
- TCS
- INFY
- HDFCBANK
- ICICIBANK
- SBIN
- ITC
- LTIM
- AXISBANK
- BHARTIARTL

The screener sorts by traded value and updates in real time when the FYERS token is valid.
