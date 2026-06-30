// npm install x402-fetch viem dotenv
require("dotenv").config();
const { wrapFetchWithPayment } = require("x402-fetch");
const { privateKeyToAccount } = require("viem/accounts");

async function main() {
    const account = privateKeyToAccount(process.env.EVM_PRIVATE_KEY);
    const fetchWithPay = wrapFetchWithPayment(fetch, account);

    const params = new URLSearchParams({
        connection: "https://dl.interzoid.com/csv/companies.csv",
        filetype: "csv",
        function: "company-name-only",
        company_column: "1",
        has_header: "true",
    });

    const res = await fetchWithPay(
        `https://match.interzoid.com/match?${params}`
    );
    console.log(await res.text());
}

main().catch(console.error);