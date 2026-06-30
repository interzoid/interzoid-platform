// npm install x402-fetch viem
import { wrapFetchWithPayment } from "x402-fetch";
import { privateKeyToAccount } from "viem/accounts";

const account = privateKeyToAccount(process.env.EVM_PRIVATE_KEY as `0x${string}`);

// Wrap fetch: the 402 quote, signing, and retry happen automatically.
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
const csv = await res.text(); // matched records, grouped by SimKey
console.log(csv);