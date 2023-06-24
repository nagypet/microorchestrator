# Micro Orchestrator

This project shows a possible implementation of a simple orchestrator. Let's say we have the below architecture of a credit transfer solution. The problem is within the `credit-transfer-service`.
![](docs/microorchestrator.png)
Whenever the frontend instructs the `credit-transfer-service` to execute the credit transfer it checks if the account balance covers the amount of the transaction, if yes, it decreases the daily limit by using the `limit-service`. Then it calls the execute() endpoint of the `giro-service`. Now the `giro-service` and the `limit-service` are independent microservices. In case the `giro-service` returns an error, all the previous actions have to be undone. For this we have the well-known SAGA pattern and Camunda as an orchestrator.

But Camunda is not free, and makes the processing pretty complicated. Can this be made easier? Let's see.
![](docs/sequence.jpg)
