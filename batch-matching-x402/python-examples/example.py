# pip install x402 eth-account httpx
import os
from eth_account import Account
from x402.clients.httpx import x402_payment_hooks
import httpx

account = Account.from_key(os.environ["EVM_PRIVATE_KEY"])

params = {
    "connection": "https://dl.interzoid.com/csv/companies.csv",
    "filetype": "csv",
    "function": "company-name-only",
    "company_column": "1",
    "has_header": "true",
}

# The x402 hooks handle the 402 quote, signing, and retry.
with httpx.Client() as client:
    client.event_hooks = x402_payment_hooks(account)
    r = client.get("https://match.interzoid.com/match", params=params)
    print(r.text)  # matched records, grouped by SimKey