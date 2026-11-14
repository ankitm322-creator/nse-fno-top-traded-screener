const express = require('express');
const axios = require('axios');
const path = require('path');
const dotenv = require('dotenv');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

const FYERS_API_URL = 'https://api-t1.fyers.in/data/v3';
const TOKEN = process.env.FYERS_ACCESS_TOKEN;
const APP_ID = process.env.FYERS_APP_ID;

const DEFAULT_SYMBOLS = [
  'NSE:RELIANCE-EQ',
  'NSE:TCS-EQ',
  'NSE:INFY-EQ',
  'NSE:HDFCBANK-EQ',
  'NSE:ICICIBANK-EQ',
  'NSE:SBIN-EQ',
  'NSE:ITC-EQ',
  'NSE:LTIM-EQ',
  'NSE:BHARTIARTL-EQ',
  'NSE:AXISBANK-EQ',
  'NSE:KOTAKBANK-EQ',
  'NSE:WIPRO-EQ',
  'NSE:ASIANPAINT-EQ',
  'NSE:MARUTI-EQ',
  'NSE:M&M-EQ',
  'NSE:SUNPHARMA-EQ',
  'NSE:NTPC-EQ',
  'NSE:POWERGRID-EQ',
  'NSE:ULTRACEMCO-EQ',
  'NSE:JSWSTEEL-EQ',
  'NSE:HINDUNILVR-EQ',
  'NSE:INDUSINDBK-EQ',
  'NSE:BAJFINANCE-EQ',
  'NSE:BAJAJ-AUTO-EQ',
  'NSE:DIVISLAB-EQ',
  'NSE:TITAN-EQ',
  'NSE:TECHM-EQ',
  'NSE:ONGC-EQ',
  'NSE:GRASIM-EQ',
  'NSE:APOLLOHOSP-EQ',
  'NSE:DRREDDY-EQ'
];

const MOCK_DATA = [
  { symbol: 'RELIANCE', ltp: 2916.4, tradedValue: 3360000000, volume: 1160000, dayChange: 1.84, high: 2936.1, low: 2876.3 },
  { symbol: 'TCS', ltp: 3880.6, tradedValue: 2415000000, volume: 630000, dayChange: 0.72, high: 3896.1, low: 3822.5 },
  { symbol: 'INFY', ltp: 1545.2, tradedValue: 2140000000, volume: 1400000, dayChange: -0.42, high: 1563.8, low: 1531.6 },
  { symbol: 'HDFCBANK', ltp: 1768.3, tradedValue: 1913000000, volume: 1090000, dayChange: 0.58, high: 1776.5, low: 1744.2 },
  { symbol: 'ICICIBANK', ltp: 1237.8, tradedValue: 1840000000, volume: 1490000, dayChange: 1.15, high: 1245.5, low: 1218.7 },
  { symbol: 'SBIN', ltp: 863.2, tradedValue: 1772500000, volume: 2080000, dayChange: 1.33, high: 867.9, low: 848.4 },
  { symbol: 'ITC', ltp: 466.7, tradedValue: 1710000000, volume: 3640000, dayChange: 0.65, high: 470.4, low: 456.8 },
  { symbol: 'BHARTIARTL', ltp: 1714.9, tradedValue: 1547000000, volume: 910000, dayChange: 1.96, high: 1732.7, low: 1684.3 },
  { symbol: 'LTIM', ltp: 6290.8, tradedValue: 1473000000, volume: 236000, dayChange: -0.18, high: 6354.5, low: 6170.8 },
  { symbol: 'AXISBANK', ltp: 1211.4, tradedValue: 1396000000, volume: 1150000, dayChange: 0.37, high: 1220.6, low: 1189.5 }
];

function toDisplayName(symbol) {
  if (!symbol) return 'Unknown';
  return symbol
    .replace(/^NSE:/, '')
    .replace(/-EQ$/, '')
    .replace(/-BE$/, '')
    .replace(/-OPT$/, '')
    .replace(/-FUT$/, '');
}

function safeNumber(value, fallback = 0) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function normalizeFyersQuote(item) {
  return {
    symbol: toDisplayName(item.symbol || item.name || item.instrument_name || 'UNKNOWN'),
    ltp: safeNumber(item.ltp ?? item.last_price ?? item.lp ?? item.close_price),
    tradedValue: safeNumber(item.vtt ?? item.trade_value ?? item.tradedvalue ?? item.traded_value ?? item.value ?? 0),
    volume: safeNumber(item.v ?? item.volume ?? item.total_volume ?? 0),
    dayChange: safeNumber(item.net_change ?? item.change ?? item.pc ?? item.percentChange ?? 0),
    high: safeNumber(item.h ?? item.high ?? item.day_high ?? 0),
    low: safeNumber(item.l ?? item.low ?? item.day_low ?? 0)
  };
}

async function queryFyersSymbols(symbols) {
  if (!APP_ID || !TOKEN) {
    return { demo: true, data: MOCK_DATA };
  }

  const hasAccess = APP_ID && TOKEN;
  if (!hasAccess) {
    return { demo: true, data: MOCK_DATA };
  }

  try {
    const response = await axios.get(`${FYERS_API_URL}/quotes`, {
      params: {
        symbols: symbols.join(',')
      },
      headers: {
        Authorization: `${APP_ID}:${TOKEN}`,
        'Content-Type': 'application/json'
      },
      timeout: 12000
    });

    const payload = response.data || {};
    const rawList = Array.isArray(payload.d) ? payload.d : Array.isArray(payload.data) ? payload.data : [];

    const normalized = rawList
      .map((item) => normalizeFyersQuote(item))
      .filter((item) => item.ltp || item.volume || item.tradedValue);

    const sorted = normalized.sort((a, b) => (b.tradedValue || 0) - (a.tradedValue || 0));
    return { demo: false, data: sorted };
  } catch (error) {
    console.error('FYERS API fetch failed:', error.message);
    return { demo: true, data: MOCK_DATA };
  }
}

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

app.get('/api/health', (_req, res) => {
  res.json({ ok: true, status: 'running' });
});

app.get('/api/top-traded-fno', async (req, res) => {
  const limit = Math.max(1, Math.min(Number(req.query.limit) || 20, 50));
  const symbols = (req.query.symbols || DEFAULT_SYMBOLS.join(','))
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean);

  const result = await queryFyersSymbols(symbols);
  const sortedData = [...result.data]
    .sort((a, b) => (b.tradedValue || 0) - (a.tradedValue || 0))
    .slice(0, limit)
    .map((entry, index) => ({
      rank: index + 1,
      symbol: entry.symbol,
      ltp: Number(entry.ltp || 0),
      tradedValue: Number(entry.tradedValue || 0),
      volume: Number(entry.volume || 0),
      dayChange: Number(entry.dayChange || 0),
      high: Number(entry.high || 0),
      low: Number(entry.low || 0),
      status: result.demo ? 'demo' : 'live'
    }));

  res.json({
    source: result.demo ? 'demo' : 'fyers',
    generatedAt: new Date().toISOString(),
    count: sortedData.length,
    data: sortedData
  });
});

app.get('*', (_req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`NSE F&O screener running on http://localhost:${PORT}`);
});
