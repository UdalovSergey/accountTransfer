# accountTransfer
####API: 
Get account by id "/accounts/_{id}_". Method **GET**

Get all accounts "/accounts". Method **GET**

Add account "/accounts". Method **POST**.  Payload { "ownerName" : "some name", "amount": 1000 }

Make a transfer "/accounts/_{accFromId}_/transfer/_{accToId}_". Method **POST**. Payload { "amount" : 500 }
