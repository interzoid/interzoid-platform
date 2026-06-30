// npm install @x402/fetch @x402/evm viem

import { x402Client, wrapFetchWithPayment } from "@x402/fetch";
import { registerExactEvmScheme } from "@x402/evm/exact/client";
import { privateKeyToAccount } from "viem/accounts";

const privateKey = process.env.EVM_PRIVATE_KEY;

if (!privateKey || !privateKey.startsWith("0x")) {
    throw new Error("EVM_PRIVATE_KEY must be set and start with 0x");
}

const signer = privateKeyToAccount(privateKey as `0x${string}`);

const client = new x402Client();
registerExactEvmScheme(client, { signer });

const fetchWithPay = wrapFetchWithPayment(fetch, client);

const params = new URLSearchParams({
    connection: "https://dl.interzoid.com/csv/companies.csv",
    filetype: "csv",
    function: "company-name-only",
    company_column: "1",
    has_header: "true",
});

const res = await fetchWithPay(
    `https://match.interzoid.com/match?${params.toString()}`
);

if (!res.ok) {
    const body = await res.text();
    throw new Error(`Request failed: ${res.status} ${res.statusText}\n${body}`);
}

const csv = await res.text();
console.log(csv);