// This examples uses a raw file on the Web (stored in AWS S3) and generates a match report
// go get github.com/x402-foundation/x402/go github.com/joho/godotenv
package main

import (
    "io"
    "net/http"
    "net/url"
    "os"

    "github.com/joho/godotenv"
    x402 "github.com/x402-foundation/x402/go"
    x402http "github.com/x402-foundation/x402/go/http"
    evm "github.com/x402-foundation/x402/go/mechanisms/evm/exact/client"
    evmsigners "github.com/x402-foundation/x402/go/signers/evm"
)

func main() {
    _ = godotenv.Load()
    signer, _ := evmsigners.NewClientSignerFromPrivateKey(os.Getenv("EVM_PRIVATE_KEY"))
    client := x402.Newx402Client().
        Register("eip155:*", evm.NewExactEvmScheme(signer, nil))
    httpc := x402http.WrapHTTPClientWithPayment(
        &http.Client{}, x402http.Newx402HTTPClient(client))

    q := url.Values{}
    q.Set("connection", "https://dl.interzoid.com/csv/companies.csv")
    q.Set("filetype", "csv")
    q.Set("function", "company-name-only")
    q.Set("company_column", "1")
    q.Set("has_header", "true")

    resp, _ := httpc.Get("https://match.interzoid.com/match?" + q.Encode())
    defer resp.Body.Close()
    body, _ := io.ReadAll(resp.Body)
    os.Stdout.Write(body) // matched records, grouped by SimKey
}