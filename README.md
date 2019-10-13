# Design

**TLDR;**

Money transfer design has been motivated for strong resilience and operations
repeatability.
Thus, it leaded to a strong separation of 'Credit' and 'Debit' operations. 
All operations have been designed to handle multiple submissions.

This allows strong idempotency, repeatability and resilience of the all operations.
If an operation fails, it will be reattempted until the feedback loop is complete.

## Transaction Id and idempotency

Money transfer is uniquely identified by a `transaction-id`. Such identifier
protects also against double submit operation (as we would do with a 'formToken').
To simplify current implementation, `transaction-id` is actually provided
by the submitter, one strongly suggest UUID type 4 generation.
In next implementation, we could ask first for a new `transaction-id`
before submitting the request.

Money transfer example:

```json
{"transaction-id": "b000e8c5-541c-45e3-9367-c785931e94f4",
 "source-id": "16b6877a-e7f1-4569-a4f4-f8bf7b4d648f",
 "destination-id": "895abadf-71d3-41ea-bdc5-4282c72eef35",
 "amount":  "500.56"}
```

*Note* : Since all amounts are stored in `BigDecimal`, one will use its
`String` representation as transferable representation.

## Debit

Once the Money transfer has been submitted, a transaction is created on the 
source 'account' and uniqueness is ensured.
As in real life, one suppose the money transfer is initiated by the (source) 
account that will give the money (the account that will be 'Debited'). 

You can submit the same transaction as many times as you want, idempotency is
ensured by transaction id.

All pending transactions are processed periodically in a serialized way,
within a single thread for a given account (The actual implementation, use
a single thread for all account, but could be easy modified): 
see [DefaultMoneyTransferService#propagateTransactions](src/main/java/banktransfer/core/account/DefaultMoneyTransferService.java#L54).
The source account is debited, transaction is marked `Debited` and the bank
should transmit the corresponding transaction to the 'destination' account's 
bank.

## Credit

The 'destination' account will receive the corresponding 'Credit' transaction 
periodically.
The 'source' bank will stop send the transaction once the transaction is marked 
`Acknowledged`. 
The transaction is stacked within the 'destination' account.
One more time, idempotency is ensured by the `transaction-id` global uniqueness,
and will be processed using the same 'by-thread' approach.

This ensures robustness: transaction can be submitted as many times as required,
preventing i/o error, etc.

## Acknowledge

The 'destination' account be will receive the 'credit' transaction as long as
it send the 'Acknowledge' back to the 'source' account.

# Implementation

## Vertx/Verticles

Application has been built on [vertx](https://vertx.io/) and `Verticle`
building block.
The `MainVerticle` sole purpose is to start the `WebVerticle` verticle, 
which is responsible to handle all http request (one should starts one
verticle per available processor minus one), and to start the `TransactionPropagationVerticle`
verticle which is the single threaded part of the transaction processing.

## Status

Due to verticle approach, and to anticipate easier error handling in asynchronus
processing, choice has been made to not rely on `Exception`. Exceptions
are catched on boundary layer (`infra`), but `core` only rely on [`Status`](src/main/java/banktransfer/core/Status.java)
construct.
`Status` can be seen as a very simplified implementation of the `Either`
(or `LeftRight`) monad.

## Usage

* [Usecases Tests](src/test/java/banktransfer/infra/UsecasesTest.java)
* [Concurrency Tests](src/test/java/banktransfer/core/account/inmemory/ConcurrencyUsecaseTest.java)


# Dev's notes

```
mvn clean package
java -jar target/bank-transfert-1.0-SNAPSHOT-fat.jar
```

Account with initial balance of `500`.

```
curl -d '{"email":"titania@tyrna.nog", "initial-balance":"500"}' \
     -H "Content-Type: application/json" \
     -X POST http://localhost:8083/account
```

```       
{"account-id":"6b827186-6cec-49b1-ab87-a16719074715"}%
```

Account with default initial balance (`0`)
```
curl -d '{"email":"oberon@tyrna.nog"}' \
     -H "Content-Type: application/json" \
     -X POST http://localhost:8083/account
```

```
{"account-id":"ee438cf0-eb3c-4aad-9f33-052179e095dd"}%
```

Transfer money between the two accounts:

```
curl -d '{"transaction-id":"99a72d4d-b862-4fee-b251-3f60d9ce7846","amount":"150", "source-id":"6b827186-6cec-49b1-ab87-a16719074715","destination-id":"ee438cf0-eb3c-4aad-9f33-052179e095dd"}' \
     -H "Content-Type: application/json" \
     -X POST http://localhost:8083/transfer
```
```
{"transaction-id":"99a72d4d-b862-4fee-b251-3f60d9ce7846"}%
```

Consult `Debited` account:

```
curl -H "Content-Type: application/json" \
     -X GET http://localhost:8083/account/6b827186-6cec-49b1-ab87-a16719074715
```
```
{"account-id":"6b827186-6cec-49b1-ab87-a16719074715",
 "balance":"350",
 "transactions":[{"transaction-id":"99a72d4d-b862-4fee-b251-3f60d9ce7846",
                  "status":"Acknowledged",
                  "source-id":"6b827186-6cec-49b1-ab87-a16719074715",
                  "destination-id":"ee438cf0-eb3c-4aad-9f33-052179e095dd",
                  "amount":"150"}]}%
```

Consult `Credited` account:

```
curl -H "Content-Type: application/json" \
     -X GET http://localhost:8083/account/ee438cf0-eb3c-4aad-9f33-052179e095dd
```

```
{"account-id":"ee438cf0-eb3c-4aad-9f33-052179e095dd",
 "balance":"150",
 "transactions":[{"transaction-id":"99a72d4d-b862-4fee-b251-3f60d9ce7846",
                  "status":"Credited",
                  "source-id":"6b827186-6cec-49b1-ab87-a16719074715",
                  "destination-id":"ee438cf0-eb3c-4aad-9f33-052179e095dd",
                  "amount":"150"}]
}%
```
