# External API

This directory contains a Flask-based external API with two endpoints:

## Endpoints

### POST /api/debin

Simulate a debit operation.

**Request Body** (JSON):

- **identifier_type** (string): The type of identifier. Allowed values: `"cvu"`.
- **identifier** (string): The CVU of the account.
- **amount** (number): The amount to debit.

**Response**:

- `200 OK`: JSON object with fields `identifier_type`, `identifier`, `amount` echoing the request.
- `400 Bad Request`: if any field is missing or invalid.

### POST /api/topup

Simulate a top-up operation.

**Request Body** (JSON):

- **identifier_type** (string): The type of identifier. Allowed values: `"cvu"`.
- **identifier** (string): The CVU of the account.
- **amount** (number): The amount to top up.

**Response**:

- `200 OK`: JSON object with fields `identifier_type`, `identifier`, `amount` echoing the request.
- `400 Bad Request`: if any field is missing or invalid.

### Example body for any endpoint:
```json
{
  "identifierType": "CVU",
  "fromIdentifier": "9999999990000000000001",
  "amount": 10
}
```
