# PayOps

**AI-assisted payment reliability and operations platform built around
PayPal Sandbox.**

PayOps demonstrates how to track payments, process work asynchronously,
analyze failures with AI, handle retries and uncertain outcomes safely,
and explain results to merchants.

> **Safety rule:** A timeout or missing response does not prove that a
> payment failed. Reconcile unknown outcomes using authoritative
> provider evidence before considering another payment operation. AI
> recommendations never independently authorize another charge.

## Product flow

``` text
Customer / Demo Store
        |
        v
     PayOps API
        |
        v
PostgreSQL: payment + durable event/outbox
        |
        v
    Payment Queue
        |
        v
Payment Worker -> PayPal Sandbox
        |
        +---- Confirmed success
        |
        +---- Failure / unexpected outcome
                         |
                         v
                Agent 1: Decision Agent
                         |
                  Backend validates
                         |
             +-----------+-------------+
             |           |             |
          Retry       Ambiguous     Non-retriable
          Queue       Workflow       failure
             |           |
             |       Agent 2: Recovery Agent
             |       (when needed)
             +-----------+-------------+
                         |
                         v
              Agent 3: Payment Summary
              (iterative, on meaningful
               lifecycle/state changes)
                         |
                         v
             payment_resolution_summary
                         |
                         v
              Agent 4: Investigation
                 & Report Agent
```

The DLQ handles processing messages that meet the configured dead-letter
policy. It is not synonymous with a payment being definitively failed.

## The four AI agents

1.  **Payment Decision Agent** --- Called when an attempt fails or
    returns an unexpected result. Uses tools to retrieve payment
    context, attempt history, sanitized logs, PayPal status/evidence,
    and retry policy. Returns a structured DTO with failure category,
    evidence, uncertainty, and a recommendation such as
    `RETRY_CANDIDATE`, `RECONCILE`, `STOP`, or `INVESTIGATE`. Backend
    policy independently validates any action.
2.  **Recovery Agent** --- Processes cases in the ambiguous-payment
    workflow or DLQ. Checks current provider evidence, payment/job
    history, prior analyses, and logs; recommends or requests
    reconciliation/recovery and saves findings. It cannot independently
    authorize another charge.
3.  **Payment Summary Agent** --- Iteratively runs after meaningful
    lifecycle changes. Reads payment state, events/audit history,
    attempts, retries, prior analyses, and recovery findings. Updates
    the evolving description in `payment_resolution_summary`. Structured
    facts remain authoritative; the AI narrative is derived.
4.  **Investigation & Report Agent** --- Answers merchant questions and
    generates reports for one payment or groups of payments. Uses
    `payment_resolution_summary` as its primary context, consulting
    detailed records when it needs verification or more detail.

Agents share persisted data and backend services; they are not tightly
coupled by calling one another's agent implementation directly.

## Queues and workflows

-   **Payment Queue:** initial asynchronous payment-processing jobs.
-   **Retry Queue:** retry jobs authorized and scheduled by the backend.
-   **Ambiguous-Payment Queue/Workflow:** cases with unknown provider
    outcomes that need reconciliation.
-   **DLQ:** processing messages rejected or exhausted under the
    configured delivery policy.

Queues carry work; PostgreSQL stores durable business state. Duplicate
delivery must be expected and handled idempotently.

## Initial data model

These are proposed responsibilities, not a requirement to create every
table immediately.

  -----------------------------------------------------------------------
  Table                               Responsibility
  ----------------------------------- -----------------------------------
  `merchants`                         Merchant identity/configuration.

  `payments`                          Source of truth for logical
                                      payment, amount/currency, provider
                                      identifiers, current state,
                                      idempotency data, and retry count.

  `payment_attempts`                  Each actual payment/provider
                                      operation, outcome, timestamps, and
                                      provider references.

  `retry_records`                     Retry ordinal, schedule/status,
                                      execution, and result.

  `dlq_records`                       Persisted dead-lettered
                                      processing-job details.

  `ambiguous_payments`                Reconciliation workflow details if
                                      a separate table is useful.

  `failure_analyses`                  Agent 1 DTOs, evidence,
                                      explanation, uncertainty, and
                                      recommendation.

  `payment_resolution_summary`        Evolving consolidated explanation
                                      used primarily by Agent 4.

  `payment_events` / `audit_records`  Lifecycle transitions, decisions,
                                      recovery actions, and outcomes.

  `outbox_events`                     Durable events for reliable
                                      RabbitMQ publication.
  -----------------------------------------------------------------------

Keep `payments` authoritative. DLQ is a job-processing state;
`AMBIGUOUS` and `FAILED_FINAL` describe payment resolution. An attempt
records an executed operation; a retry record tracks retry
scheduling/execution. Evolve the schema with migrations as workflows are
implemented.

## Technology stack

-   Backend: Java 21, Spring Boot, Maven
-   Database: PostgreSQL
-   Messaging: RabbitMQ / Spring AMQP
-   Local infrastructure: Docker Compose
-   Payment provider: PayPal Sandbox
-   Frontend: Next.js + TypeScript
-   AI: four specialized agents with narrowly scoped backend tools
-   Notifications: provider to be selected during implementation

Verify current official PayPal documentation for checkout/capture,
webhook verification, idempotency, and reconciliation before
implementing provider-specific behavior.

## Reliability principles

1.  Unknown is not failed: reconcile before considering another payment
    operation.
2.  AI is advisory; backend policy and authoritative evidence authorize
    actions.
3.  Use idempotency for merchant requests, message consumers, webhooks,
    and notifications.
4.  Expect duplicate broker delivery and make consumers safe to repeat.
5.  Use a transactional outbox for reliable database-to-RabbitMQ
    publication.
6.  Do not hold database transactions open during PayPal, AI, or email
    network calls.
7.  Store original evidence separately from AI interpretation.
8.  Keep payment status separate from background-job/DLQ status.
9.  Audit decisions and recovery actions.
10. Redact secrets and unnecessary personal/payment data from logs and
    AI context.

## Development status and setup notes

This is a learning project under incremental development; do not assume
all planned workflows are implemented. Previously established setup
includes Java 21, a Spring Boot backend at `D:\Payops\payment-service`,
Maven Wrapper, and Docker Compose services for PostgreSQL and RabbitMQ.
The last known issue was PostgreSQL authentication/instance mismatch on
port `5432`; verify which server is responding before changing
credentials. RabbitMQ still needed integration into the Spring Boot
workflow. Verify the current repository before relying on these notes.

## Getting started

1.  Inspect the current repository and configuration before changing
    anything.
2.  Start Docker Desktop and verify PostgreSQL/RabbitMQ with
    `docker compose ps`.
3.  Confirm which PostgreSQL instance answers on port `5432` and verify
    application credentials.
4.  Run the backend using the Maven Wrapper once the datasource
    connection is confirmed.
5.  Implement and test one workflow at a time, starting with payment
    persistence and a safe PayPal Sandbox flow.

Keep credentials in environment variables or a local `.env` excluded
from version control.

## Interview summary

PayOps explores payment reliability, not just payment API integration:
preserving the payment lifecycle, handling asynchronous failures,
distinguishing uncertain outcomes from confirmed failures, using AI for
evidence-based analysis, and providing a consolidated explanation and
investigation interface. Four AI responsibilities are separated while
payment authorization and state transitions remain under deterministic
backend control.
