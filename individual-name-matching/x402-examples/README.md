# Interzoid API Access via x402 Blockchain Payments (BASE Network)

This directory contains **x402 protocol integration examples** for accessing **Interzoid Data Quality APIs** using blockchain-based micropayments on the **BASE** network. For more information about Interzoid's API platform, please visit:
https://www.interzoid.com

These examples help developers, data engineers, and Web3 builders call Interzoid's AI-powered data quality, data matching, and data enrichment APIs and **pay per request using USDC on the BASE blockchain** — no API key or subscription required.

---

## Overview

The **x402 protocol** enables machine-to-machine payments over HTTP using the standard `402 Payment Required` status code. Instead of authenticating with a traditional API key, a client pays for each API call with a small USDC micropayment on the BASE blockchain (an Ethereum Layer 2 network). This opens up a new paradigm for API access:

- **No API key signup or subscription required** — pay per call with USDC
- **Ideal for AI agents and autonomous systems** that need programmatic API access without human-managed credentials
- **Instant settlement** on the BASE blockchain using USDC (a stablecoin pegged to the US Dollar)
- **Fully compatible** with all Interzoid data quality, matching, and enrichment APIs

The x402 payment flow is handled transparently by the client library — your code makes a standard `fetch` request, and the library automatically negotiates and signs the blockchain payment when the server responds with `402 Payment Required`.

---

## How x402 Works with Interzoid APIs

Interzoid APIs support **dual access modes**:

1. **Traditional API Key** — Include a `license` parameter with your API key (standard subscription model).
2. **x402 Blockchain Payment** — Omit the `license` parameter, and the server responds with a `402 Payment Required` containing payment terms if the other parameters are correct. The x402 client library handles the rest automatically.

### The x402 Payment Flow

1. **Client sends a request** to an Interzoid API endpoint *without* the `license` parameter.
2. **Server responds with `402 Payment Required`**, including a JSON body that specifies the payment terms: amount, asset (USDC), network (BASE), and the recipient wallet address.
3. **Client library signs a payment** using the caller's private wallet key and retries the request with payment proof attached.
4. **Server verifies the payment** on-chain and returns the API response.

### Example: Triggering a 402 Response

Calling an Interzoid API without the `license` API key parameter triggers the x402 payment flow:

```
GET https://api.interzoid.com/getfullnamematch?fullname=Jenny%20Smith
```

The server responds with `402 Payment Required` and a JSON body like:

```json
{
  "x402Version": 2,
  "accepts": [
    {
      "scheme": "exact",
      "network": "eip155:8453",
      "maxAmountRequired": "12500",
      "amount": "12500",
      "resource": "/getfullnamematch?fullname=Jenny%20Smith",
      "description": "Full name match similarity key",
      "mimeType": "application/json",
      "payTo": "0xdCEca23FF8A7145e1b5B35427C9886CF21A67566",
      "maxTimeoutSeconds": 60,
      "asset": "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913",
      "decimals": 6,
      "extra": {
        "name": "USD Coin",
        "version": "2"
      }
    }
  ],
  "error": "Payment Required"
}
```

Key fields in the 402 response:

| Field | Description |
|-------|-------------|
| `network` | `eip155:8453` — the BASE blockchain (Ethereum L2) |
| `amount` | Cost in USDC base units (6 decimals). `12500` = **$0.0125 USD** |
| `asset` | `0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913` — USDC contract on BASE |
| `payTo` | Wallet address that receives the payment |
| `scheme` | `exact` — the payment scheme used for signing |

---

## What This Directory Demonstrates

The Node.js example in this directory shows how to:

- call any Interzoid API using x402 blockchain micropayments instead of an API key
- configure a wallet signer using a private key
- use the `@x402/fetch` and `@x402/evm` client libraries to handle the payment flow automatically
- inspect the API response and on-chain settlement details

---

## Prerequisites

- **Node.js** (v18 or later recommended)
- **A BASE-compatible wallet** with a small amount of USDC for payments
- **A private key** for the wallet (stored securely in a `.env` file)

### Install Dependencies

```bash
npm install dotenv @x402/fetch @x402/evm viem
```

### Configure Your Wallet Key

Create a `.env` file in this directory:

```
PRIVATE_KEY=0xYOUR_PRIVATE_WALLET_KEY_HERE
```

> ⚠️ **Never commit your private key to version control.** Add `.env` to your `.gitignore` file.

---

## Running the Example

The file **`interzoid-x402-client.js`** demonstrates a complete x402 payment flow calling the Interzoid Full Name Match API:

```bash
node interzoid-x402-client.js
```

### What the Example Does

1. **Loads your private key** from the `.env` file and creates a wallet signer using Viem.
2. **Initializes the x402 client** and registers the EVM payment scheme for BASE.
3. **Wraps the standard `fetch` function** with automatic x402 payment handling.
4. **Calls the Interzoid API** — the library detects the `402` response, signs a USDC payment on BASE, and retries the request.
5. **Displays the API result** (a similarity key for the input name) and the on-chain settlement details.

### Expected Output

```
🤖 Starting x402 Client Test (v2)...
💳 Paying with: 0xYourWalletAddress...
1️⃣  Hitting Server...

✅ SUCCESS! Payment Accepted.
------------------------------------------------
{
  "Code": "Success",
  "Credits": "0",
  "SimKey": "xY2zAb4cDe8fGh1i"
}

💰 Settlement Details:
{
  ...on-chain transaction details...
}
```

---

## Supported Interzoid APIs

All Interzoid APIs that accept a `license` parameter also support x402 payments. Simply omit the `license` parameter and use the x402 client. Examples include:

- **Individual Name Matching** — `getfullnamematch`
- **Company Name Matching** — `getcompanymatchadvanced`
- **Address Matching** — `getaddressmatchadvanced`

Full API catalog: https://www.interzoid.com/cloud-api-directory

---

## Common Use Cases for x402 API Access

### AI Agents and Autonomous Systems
- enable AI agents to access data quality APIs without pre-provisioned API keys
- support autonomous data pipelines that pay for what they use in real time

### Pay-Per-Use Access
- access Interzoid APIs without a subscription or account signup
- ideal for testing, prototyping, or low-volume usage

### Web3 and Decentralized Applications
- integrate data quality capabilities into dApps with native USDC payments
- build composable data services in blockchain-native architectures

### Machine-to-Machine Commerce
- enable automated systems to negotiate and pay for API services programmatically
- support the emerging M2M economy with standards-based payment protocols

---

## How It Works (Technical Detail)

1. The x402 client library wraps the standard `fetch` function.
2. When a request returns `402 Payment Required`, the library parses the payment terms from the response body.
3. The library uses your private key to sign an on-chain USDC payment on the BASE network to the specified `payTo` address.
4. The signed payment proof is attached to a retry of the original request.
5. The server verifies the payment and returns the API data.

All of this happens transparently — your application code simply calls `fetchWithPayment(url)` as if it were a normal `fetch`.

---

## Resources

- **Interzoid API Documentation**: https://www.interzoid.com/cloud-api-directory
- **x402 Protocol**: https://www.x402.org
- **BASE Network**: https://base.org
- **USDC on BASE**: https://www.circle.com/usdc
- **Viem (Ethereum Library)**: https://viem.sh
