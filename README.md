1   Introduction
As part of our technical evaluation process, we would like you to complete a small backend development assignment using Spring Boot.
The purpose of this assignment is to understand how you approach practical backend engineering problems, design APIs, work with external services, model data, implement business logic, and ensure software quality.
We are interested not only in the final functionality but also in your technical decisions, code quality, maintainability, and testing approach.
You are free to make your own technical decisions unless a specific requirement is explicitly stated.
What we expect. We do not expect a production-ready enterprise application. The goal is to demonstrate your engineering approach through a focused implementation.
2   Project Overview – Money Transfer Service
Imagine that we are building the backend of a small money transfer application. Users can open accounts, each in a specific currency such as EUR or USD, and transfer money from one account to another.
EXAMPLE
Anna has a EUR account with a balance of 1,000. She sends 200 to Ben’s EUR account. After the transfer, Anna’s balance is 800 and Ben’s balance has increased by 200.

Accounts do not have to use the same currency. When money is sent to an account in a different currency, the amount is converted using the current exchange rate from an external service.
EXAMPLE
Anna sends 100 from her EUR account to Ben’s USD account. If the current exchange rate is 1 EUR = 1.10 USD, Anna’s balance decreases by 100 EUR and Ben receives 110 USD.

Every transfer is checked against predefined rules. A transfer that breaks a rule is rejected, and the user is told why.
3   Functional Requirements
The application should support:
•	Creating users.
•	Creating accounts for a user. A user can have several accounts, each in a specific currency and with an optional opening balance.
•	Viewing an account with its current balance.
•	Transferring money between accounts.
•	Converting the amount, using an external currency exchange service, when the accounts use different currencies.
•	Viewing the transfer history of an account.
3.1   Money Transfers
A transfer specifies the source account, the target account, and the amount, which is expressed in the currency of the source account.
A transfer must be between two different accounts, for an amount greater than zero, and the source account must have a sufficient balance.
When a transfer succeeds, the response should include the amount sent, the amount received, and the exchange rate used if a conversion took place.
3.2   Transfer Rules
Every transfer is checked against the following rules. A transfer that violates a rule is rejected.
Rule	The transfer is rejected when…
Single transfer limit	The amount is more than 1,000.
Daily limit	The source account’s successful transfers today, including this one, add up to more than 2,500.
Cross-currency limit	The accounts use different currencies and the amount is more than 500.

EXAMPLE
A user sends 1,200 from a EUR account to a USD account. The single transfer limit and the cross-currency limit are both violated, so the transfer is rejected.

When a transfer is rejected, the response should return the rules that were violated, using the error response model described in section 4.1.
3.3   Currency Conversion
When the two accounts use different currencies, the application must retrieve the current exchange rate from an external currency exchange service and calculate the amount the target account receives.
You are free to choose any publicly available exchange rate API.
If an exchange rate is unavailable for the requested currencies, the application should handle this situation gracefully.
3.4   Transfer History
Each transfer, whether successful or failed, should be stored in the history of the account.
For each transfer, the history should show the amount sent and its currency, the currency exchange if one took place, and the status of the transfer.
4   Technical Expectations
We expect the solution to demonstrate good backend engineering practices.
You are free to choose the architecture, libraries, and implementation techniques you consider appropriate.
Out of scope. Authentication and authorization are outside the scope of this assignment.
4.1   API
The APIs of the application should be clear and explicit, so that clients can easily understand how to use them.
Errors must be returned using the following error response model:
•	trace-id: A unique identifier of the request, to help trace it.
•	status: The HTTP status code of the error.
•	message: A clear explanation of what went wrong.

EXAMPLE ERROR RESPONSE
{
"trace-id": "7c1d9e2a-4b6f-4f0e-9c3a-2e5b8a1d6f40",
"status": 422,
"message": "Rules violated: Single transfer limit, Cross-currency limit"
}
4.2   Data Persistence
The application should persist relevant information in a relational database.
Each transfer that involved a currency conversion should retain the exchange rate used, so that previously processed transfers can still be understood later.
You are responsible for deciding how to model and persist the required information.
4.3   Testing
Testing is an important part of this assignment. Please include unit and integration tests.
5   Final Note
There is no single expected implementation or architectural solution.
We are interested in understanding how you approach the problem, how you make technical decisions, and how you balance simplicity, maintainability, and extensibility.
Please be prepared to explain your implementation and the reasoning behind your key decisions during the technical discussion.
Submission. Please share a link to your repository with us (GitHub, GitLab, or any other). Another developer should be able to clone it and run the application by following your instructions.

Endpoint 
 Success response 
 Example error response 
 POST /api/users 
 201 — {"id":1,"name":"Anna"} 
 400 — {"trace-id":"<uuid>","status":400,"message":"Name is required"} 
 POST /api/users/{userId}/accounts 
 201 — {"id":1,"userId":1,"currency":"EUR","balance":1000} 
 404 — {"trace-id":"<uuid>","status":404,"message":"User not found: 999"} 
 GET /api/accounts/{accountId} 
 200 — {"id":1,"userId":1,"currency":"EUR","balance":1000} 
 404 — {"trace-id":"<uuid>","status":404,"message":"Account not found: 999"} 
 POST /api/transfers 
 200 — {"id":1,"sourceAccountId":1,"targetAccountId":2,"amount":100,"currency":"EUR","amountReceived":110.0,"targetCurrency":"USD","exchangeRate":1.10,"status":"SUCCESS","createdAt":"2026-11-06T10:15:30","failureReasons":null} 

What’s included:
•User creation
•Account creation per user, with currency and optional opening balance
•Account lookup
•Money transfer API with validation rules:
◦amount > 0
◦different source/target account
◦sufficient balance
◦single transfer limit: > 1000
◦daily limit: successful transfers today > 2500
◦cross-currency limit: different currencies and amount > 500
•External FX lookup via Frankfurter API
•Transfer history persistence
•Error responses using the required trace-id, status, and message model
•JPA/H2 persistence for users, accounts, and transfers
•Unit + integration tests
