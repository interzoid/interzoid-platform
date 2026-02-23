require('dotenv').config();
const { x402Client, wrapFetchWithPayment } = require('@x402/fetch');
const { registerExactEvmScheme } = require('@x402/evm/exact/client');
const { privateKeyToAccount } = require('viem/accounts');

// CONFIGURATION of Interzoid API call
const SERVER_URL = 'https://api.interzoid.com/getfullnamematch?fullname=Jenny%20Smith';

// Make sure this private wallet key to send payment is set in your .env file
const PRIVATE_KEY = '[private-wallet-key]'; 

async function runTest() {
    console.log("🤖 Starting x402 Client Test (v2)...");

    if (!PRIVATE_KEY) {
        console.error("❌ Error: TEST_WALLET_KEY is missing from .env");
        return;
    }

    try {
        // 1. Setup Signer (using Viem)
        const account = privateKeyToAccount(PRIVATE_KEY);
        console.log(`💳 Paying with: ${account.address}`);

        // 2. Initialize Client
        const client = new x402Client();

        // 3. Register the EVM Scheme
        // This tells the client how to sign EVM payments
        registerExactEvmScheme(client, { signer: account });

        // 4. Wrap the standard 'fetch' function
        // This automatically handles: request -> 402 -> sign -> retry
        const fetchWithPayment = wrapFetchWithPayment(fetch, client);

        console.log("1️⃣  Hitting Server...");


        const response = await fetchWithPayment(SERVER_URL);

        // 5. Output Results
        if (response.ok) {
            const data = await response.json();
            console.log("\n✅ SUCCESS! Payment Accepted.");
            console.log("------------------------------------------------");
            console.log(JSON.stringify(data, null, 2));

            // Check for payment response header
            const paymentResponse = response.headers.get('payment-response');
            if (paymentResponse) {
                const decoded = JSON.parse(
                    Buffer.from(paymentResponse, 'base64').toString()
                );
                console.log("\n💰 Settlement Details:");
                console.log(JSON.stringify(decoded, null, 2));
            }
        } else {
            console.log(`\n❌ FAILED. Status: ${response.status}`);
            const text = await response.text();
            console.log(text);
        }

    } catch (error) {
        console.error("\n❌ ERROR:", error.message);
        if (error.cause) console.error("  Cause:", error.cause);
    }
}

runTest();
