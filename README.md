# Parking Reservation System – Code Challenge

**LICENSE**
Copyright © ParkHere GmbH – All Rights Reserved.
Unauthorized copying of any content of this project via any medium is strictly prohibited. Proprietary and confidential.

---

## Introduction

Welcome, and thanks for taking the time to work on this challenge!

Please read this document in full before starting. Your goal is to implement a parking reservation system composed of loosely coupled microservices. The solution should reflect the general principles of high-demand distributed systems: **scalability**, **availability**, and **reliability**.

### Requirements

- Use **Java 11+** or **Kotlin**.
- The application must build and run successfully.
- Your solution must include sufficient tests or demonstration to confirm correctness.

### Notes

- You are free to use any application framework (e.g. Quarkus, Spring Boot).
- Deploying your solution on **AWS** using any suitable service (e.g. Lambda, ECS, EKS) is considered an advantage.
- You are free to choose any database or messaging middleware that best fits the requirements. We'd love to hear your reasoning, so please justify your choices briefly in a README.

---

## Context

ParkHere is building a product that allows users to reserve parking spots in a car park via a mobile app before they arrive. Upon arrival, the user is directed to their reserved spot without any manual check-in.

The target response time for the reservation service is **≤ 500ms** under normal load, and the system must be designed to scale across multiple countries and car parks.

---

## Part 1: Reservation Service (Required)

The system involves two microservices. You must **implement** the `reservation-service`. The `configuration-microservice` is **provided** — you don't need to implement it, but please define the interface your implementation expects from it (see section 1.1 below).

### 1.1 Configuration Microservice (Provided)

This service exposes parking spot data for a given car park.

**Endpoint:**

```
GET /api/parking-lots/{parking-lot-id}
```

**Response:**

```json
[
  {
    "id": 1,
    "spotName": "spot1",
    "priority": 1
  }
]
```

- `id`: unique identifier of the parking spot.
- `spotName`: human-readable label for the spot.
- `priority`: integer used to rank spots for assignment. **Lower value = higher priority** (i.e. priority `1` is assigned before priority `2`).

A sample JSON response file is included in the repository.

> **If you consume this service**, please specify the full interface you expect from it (endpoint, request/response contract, and relevant error cases) in your README or as an interface/contract file.

---

### 1.2 Reservation Service (To Be Implemented)

Implement a service that exposes the following endpoint:

**Endpoint:**

```
POST /api/parking-lots/{parking-lot-id}/reservations
```

**Request body:**

```json
{
  "userId": "john@park-here.eu",
  "startTimestamp": 1737586800000,
  "endTimestamp": 1737627502000
}
```

**Success response** (`201 Created`):

```json
{
  "id": 123,
  "spotId": 1,
  "userId": "john@park-here.eu",
  "startTimestamp": 1737586800000,
  "endTimestamp": 1737627502000
}
```

**Error responses:**

- `400 Bad Request` – if `startTimestamp >= endTimestamp` or required fields are missing.
- `409 Conflict` – if no spots are available for the requested time frame.

#### Business Rules

1. **Reservations must be persisted permanently** (i.e. they must survive service restarts).
2. `startTimestamp` must be strictly less than `endTimestamp`. Both are Unix timestamps in milliseconds (UTC).
3. A user may hold multiple reservations, **as long as their time frames do not overlap**.
4. **No two reservations may share the same spot if their time frames overlap.** Two time frames overlap if one starts before the other ends.
5. When assigning a spot, the service must select the **available spot with the lowest priority value**. If all spots are taken for the requested time frame, return `409` with a descriptive error message.

---

## Part 2: System Design (Required)

Using a diagramming tool of your choice (e.g. [draw.io](https://draw.io), Lucidchart, Excalidraw), sketch out the major components of your system. This could include the microservices and how they interact, your database choices, and any additional infrastructure you'd consider relevant (e.g. an API gateway, message broker, or cache). Feel free to add any notes explaining your thinking.

Please include an image of your diagram in the repository, or share a link to the online tool.

---

## Part 3: Deployment Plan (Optional – Bonus)

If you'd like to go the extra mile, describe a deployment plan for your solution on a cloud provider. **AWS is preferred.**

At ParkHere, we use **Terraform** for infrastructure provisioning. A strong bonus submission would include:

- A Terraform configuration (or a clearly structured outline) covering the core infrastructure components.
- A brief explanation of your deployment strategy (e.g. ECS, EKS, Lambda) and why it suits this use case.
- Some consideration of environment separation (e.g. staging vs. production).

---

## Evaluation Criteria

Here's what we'll be looking at when reviewing your submission:

- **Correctness** – business rules are properly enforced.
- **Code quality** – clean, readable, well-structured, and maintainable code.
- **Design decisions** – appropriate use of patterns, data models, and service boundaries.
- **Concurrency handling** – correct behaviour under concurrent reservation requests.
- **Documentation** – a clear README covering setup, design decisions, and assumptions.

> Don't feel the need to over-engineer. A focused, well-reasoned solution is valued far more than an unnecessarily complex one.

---

## Submission Instructions

1. Fork this private repository to a **new private repository** on a Git provider of your choice.
2. Complete your implementation in the forked repository.
3. Include the architecture diagram (image or link) in the repository.
4. Include a `README.md` with:
   - Setup and run instructions.
   - A summary of your design decisions and trade-offs.
   - Any assumptions you made along the way.
5. Once you're done, grant review access to the following colleagues:
   - andac.kurun@park-here.eu
   - jakob.mezger@park-here.eu
   - mirzet.brkic@park-here.eu
   - massimiliano.gerardi@park-here.eu

We're looking forward to seeing your solution — good luck!
**ParkHere Engineering Team**
