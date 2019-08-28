# Account transfer
#### API: 
Get account by id "/accounts/_{id}_". Method **GET**

Get all accounts "/accounts". Method **GET**

Add account "/accounts". Method **POST**.  Payload { "ownerName" : "some name", "amount": 1000 }

Make a transfer "/accounts/_{accFromId}_/transactions/". Method **POST**. Payload { "accToId" : 1, "amount" : 500 }

#### What does it consist of

**Database** - based on the *ConcurrentHashMap*. To prevent direct access 
to objects into the Map, the Repository returns copy of objects.

**Transaction Executor** - It is a transaction processor, which grabs
 transactions from the queue and then process them in parallel. 
 I don't want to use Guava (striped) for associating a lock with an object, 
 so I did my own simple implementation of it.

**Service layer** - provides basic logic for managing transaction and accounts.

**HTTP Server** - based on *Jetty*. It doesn't have easy 
way to create endpoints and error handling, so I had to reinvent the wheel there.    

#### How to test

**TransactionExecutorIntegrationTest** - A concurrent test for transaction creation and execution.
It creates transactions in parallel, and waits for the all transactions are processed by the Executor.
 
**AccountHandlerIntegrationTest** - A test for the API. it starts the HTTP server and the Transaction Executor during the test. 


#### How to build
mvn assembly:assembly