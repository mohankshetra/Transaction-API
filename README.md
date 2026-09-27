# Transaction API

A Spring Boot REST API that:
1. Accepts a file and an amount as input.
2. Validates that the amount falls between **Rs. 100** and **Rs. 1,00,000** (inclusive).
3. If valid, marks the transaction successful, generates a transaction ID, and **appends
   it to the uploaded file**.
4. If invalid, returns a `400 Bad Request` with an explanation — no file is touched.

## Project layout

```
txn-api/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── src/main/java/com/example/txnapi/
    ├── TxnApiApplication.java
    ├── controller/TransactionController.java
    ├── service/TransactionService.java
    └── model/TransactionResponse.java
```

## Run with Docker (recommended)

```bash
docker compose up --build
```

This builds the image (multi-stage Maven build → slim JRE runtime) and starts the API on
`http://localhost:8080`, with `/data/transactions` persisted in a named Docker volume so
files survive container restarts.

Alternatively, without compose:

```bash
docker build -t txn-api .
docker run -p 8080:8080 -v txn-data:/data/transactions txn-api
```

## Run locally without Docker

```bash
mvn clean package
java -jar target/txn-api.jar
```

## API

### POST `/api/transactions`

`multipart/form-data` body:

| Field  | Type   | Description                              |
|--------|--------|-------------------------------------------|
| file   | file   | The file to append the transaction ID to  |
| amount | number | Amount to transact (100 – 100000)         |

**Example — valid amount:**

```bash
curl -X POST http://localhost:8080/api/transactions \
  -F "file=@sample.txt" \
  -F "amount=5000"
```

Response:

```json
{
  "success": true,
  "transactionId": "TXN-3f2a9c1e-...",
  "fileName": "sample.txt",
  "message": "Transaction successful. Transaction ID appended to file."
}
```

The stored copy of `sample.txt` (inside the container's `/data/transactions` volume) now
has a line like:

```
TransactionId: TXN-3f2a9c1e-... | Amount: Rs.5000 | Timestamp: 2026-09-26T10:15:30
```

**Example — amount out of range:**

```bash
curl -X POST http://localhost:8080/api/transactions \
  -F "file=@sample.txt" \
  -F "amount=50"
```

Response (`400 Bad Request`):

```json
{
  "success": false,
  "transactionId": null,
  "fileName": null,
  "message": "Amount must be between Rs.100 and Rs.1,00,000 (inclusive)"
}
```

### GET `/api/transactions/download/{fileName}`

Downloads the processed file back, so you can confirm the transaction ID was appended.

```bash
curl -O http://localhost:8080/api/transactions/download/sample.txt
```

## Notes / things you may want to extend

- The transaction ID is a random UUID (`TXN-<uuid>`) generated in-memory — there's no
  persistence layer (DB) recording transactions separately from the file itself. Add a
  database (e.g. H2/Postgres + Spring Data JPA) if you need a queryable transaction log.
- Currently, uploading a file with the same name overwrites the previous stored copy
  before appending. If you need per-upload versioning instead, generate a unique storage
  path per request.
- No authentication/authorization is included — add Spring Security if this will be
  exposed beyond local/internal use.
