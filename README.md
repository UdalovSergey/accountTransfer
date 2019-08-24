# accountTransfer
API:
Get account by id "/accounts/{id}". Method GET
Get all accounts "/accounts". Method GET
Add account "/accounts". Method POST.  Payload { "ownerName" : "some name", "amount": 1000 }
Make a transfer "/accounts/{accFromId}/transfer/{accToId}". Method POST. Payload { "amount" : 500 }
