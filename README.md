# 🛒 Ecommerce System API

A full-featured e-commerce backend API covering the complete purchase lifecycle — from product management through order creation, checkout, payment processing, and webhook handling.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
  - [Products](#products)
  - [Orders](#orders)
  - [Checkout](#checkout)
  - [Payments](#payments)
  - [Webhooks](#webhooks)
- [Flow Diagram](#flow-diagram)
- [Idempotency](#idempotency)
- [Payment Integration](#payment-integration)
- [Environment Variables](#environment-variables)
- [Postman Collection](#postman-collection)

---

## Overview

This API powers a full e-commerce transaction flow:

```
Product → Order (Reserve Inventory) → Checkout → Payment → Webhook Confirmation
```

It supports manual payment initiation, payment status polling, and automated webhook callbacks from payment providers (PhonePe supported out of the box).

---

## Tech Stack

> Update this section to match your actual stack.

- **Runtime:** Java / Spring Boot *(assumed from port 8080)*
- **Base URL:** `http://localhost:8080/api/v1`
- **Payment Provider:** PhonePe (UAT + Production)
- **Auth:** Idempotency Key (on checkout), Vault-managed JWT (PhonePe)

---

## Getting Started

### Prerequisites

- Java 17+ (or your runtime)
- A running instance of the backend service
- PhonePe merchant credentials (for payment flows)

### Run Locally

```bash
# Clone the repository
git clone https://github.com/your-org/ecommerce-system-api.git
cd ecommerce-system-api

# Start the server (example for Spring Boot)
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

---

## API Reference

### Base URL

```
http://localhost:8080/api/v1
```

---

### Products

#### Create a Product

```http
POST /products
Content-Type: application/json
```

**Request Body:**

```json
{
  "name": "Sony Xperia 1 V",
  "price": 119999,
  "quantity": 3
}
```

| Field      | Type    | Description                         |
|------------|---------|-------------------------------------|
| `name`     | string  | Product name                        |
| `price`    | integer | Price in smallest currency unit (paise) |
| `quantity` | integer | Available stock quantity            |

---

#### Get Product by ID

```http
GET /products/{productId}
```

| Parameter   | Type   | Description        |
|-------------|--------|--------------------|
| `productId` | string | The product's ID   |

---

#### Get Product by UUID

```http
GET /api/v1/products/{uuid}
```

| Parameter | Type   | Description         |
|-----------|--------|---------------------|
| `uuid`    | string | The product's UUID  |

---

#### Search Products by Name

```http
GET /products/search?name={query}
```

| Query Param | Type   | Description               |
|-------------|--------|---------------------------|
| `name`      | string | Partial or full product name |

**Example:**
```
GET /products/search?name=Apple
```

---

#### Get Available Products

```http
GET /products/available
```

Returns all products currently in stock (quantity > 0).

---

### Orders

#### Create Order (Reserve Inventory)

```http
POST /orders
Content-Type: application/json
```

**Request Body:**

```json
{
  "items": [
    {
      "productId": "{{productId}}",
      "quantity": 2,
      "price": 79999
    }
  ]
}
```

| Field              | Type    | Description                          |
|--------------------|---------|--------------------------------------|
| `items[].productId`| string  | ID of the product to order           |
| `items[].quantity` | integer | Number of units                      |
| `items[].price`    | integer | Price per unit at time of order      |

> ⚠️ This step **reserves inventory**. The stock is held until payment is completed or the order expires.

---

### Checkout

#### Checkout (Order + Payment in One Step)

```http
POST /checkout
Content-Type: application/json
Idempotency-Key: {{idempotencyKey}}
```

**Request Body:**

```json
{
  "items": [
    {
      "productId": "{{productId}}",
      "quantity": 1
    }
  ],
  "provider": "PHONEPE"
}
```

| Field              | Type    | Description                         |
|--------------------|---------|-------------------------------------|
| `items[].productId`| string  | ID of the product                   |
| `items[].quantity` | integer | Number of units                     |
| `provider`         | string  | Payment provider (`PHONEPE`)        |

**Headers:**

| Header            | Description                                      |
|-------------------|--------------------------------------------------|
| `Idempotency-Key` | Unique key to prevent duplicate payment creation |

> 💡 The `Idempotency-Key` is auto-generated in the Postman collection using the format `{userId}:{operation}:{timestamp}:{uuid}`.

---

### Payments

#### Initiate Payment (Manual)

Use this when you have an existing order and want to trigger payment separately from checkout.

```http
POST /payments
Content-Type: application/json
```

**Request Body:**

```json
{
  "orderId": "{{orderId}}",
  "provider": "PHONEPE"
}
```

---

#### Check Payment Status (Polling)

```http
POST /payments/status/{merchantOrderId}
```

| Parameter         | Type   | Description                              |
|-------------------|--------|------------------------------------------|
| `merchantOrderId` | string | Merchant-side order ID from payment init |

Poll this endpoint to check if a payment has been completed, is pending, or has failed.

---

### Webhooks

Webhooks are called by the payment provider to notify your system of payment state changes.

#### Payment Success

```http
POST /webhook/phonepe
Content-Type: application/json
```

```json
{
  "merchantOrderId": "{{merchantOrderId}}",
  "state": "COMPLETED"
}
```

#### Payment Failed

```http
POST /webhook/phonepe
Content-Type: application/json
```

```json
{
  "merchantOrderId": "{{merchantOrderId}}",
  "state": "FAILED"
}
```

| `state` Value | Description                              |
|---------------|------------------------------------------|
| `COMPLETED`   | Payment succeeded; fulfill the order     |
| `FAILED`      | Payment failed; release reserved stock   |

---

## Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                   E-Commerce Flow                        │
└─────────────────────────────────────────────────────────┘

  [1] POST /products          → Create product + set stock
         │
  [2] GET /products/{id}      → Verify product details
         │
  [3] POST /orders            → Reserve inventory
         │
         ├──── Option A: Separate Payment ────────────────┐
         │     [5] POST /payments                         │
         │     [6] POST /payments/status/{merchantOrderId}│ (poll)
         │                                                │
         └──── Option B: Checkout (All-in-one) ──────────┘
               [4] POST /checkout (with Idempotency-Key)
                       │
               [7/8] POST /webhook/phonepe
                       │
               state = COMPLETED → Fulfill Order
               state = FAILED    → Release Inventory
```

---

## Idempotency

The `/checkout` endpoint requires an `Idempotency-Key` header to prevent duplicate payment requests on retries.

**Key format:**
```
{userId}:{operation}:{unixTimestamp}:{uuidv4}
```

**Example:**
```
u123:PAYMENT:1718000000:a3f5d2c1-8e4b-47a9-bcd3-1234567890ab
```

The Postman pre-request script generates this automatically using `crypto.randomUUID()`.

---

## Payment Integration

### PhonePe (UAT)

The collection includes a direct PhonePe UAT payment page request:

```http
GET https://mercury-uat.phonepe.com/transact/uat_v3?token={{vault:json-web-token}}&routingKey=W
```

- The JWT is managed via **Postman Vault** (`{{vault:json-web-token}}`).
- `routingKey=W` routes to the wallet payment method in UAT.
- The response is rendered as HTML using `pm.visualizer.set(html)` in the Postman test script.

> 🔐 Never hardcode your PhonePe JWT. Use environment secrets or a secrets manager in production.

---

## Environment Variables

Configure these in your Postman environment or `.env` file:

| Variable          | Description                                    | Example                        |
|-------------------|------------------------------------------------|--------------------------------|
| `baseUrl`         | Base URL of the API server                     | `http://localhost:8080/api/v1` |
| `productId`       | ID returned after creating a product           | `prod_abc123`                  |
| `orderId`         | ID returned after creating an order            | `ord_xyz789`                   |
| `merchantOrderId` | Merchant order ID from the payment initiation  | `mer_ord_456`                  |
| `uuid`            | UUID for product lookup                        | `a1b2c3d4-...`                 |
| `idempotencyKey`  | Auto-generated key for checkout                | *(auto-set by pre-request)*    |

---

## Postman Collection

Import the collection directly into Postman:

[![Run in Postman](https://run.pstmn.io/button.svg)](https://go.postman.co/collection/36130258-dda97227-07bf-47a7-a5ee-5794f0152ced?source=collection_link)

Or import the file manually:

1. Open Postman → **Import**
2. Select `Ecommerce_System_API_postman_collection.json`
3. Set up your environment variables (see above)
4. Run requests in order: **1 → 2 → 3 → 4 (or 5+6) → 7 or 8**

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

This project is licensed under the [MIT License](LICENSE).
