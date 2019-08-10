# Transfer Service

## Prerequisites
* Java 8
* Maven

## Running the service via Maven
1. Checkout the project
2. Go to transfer-service directory
    ```
    # In transfer-service directory
    $ mvn clean package exec:java -U 
    ```
   
## Running the service via executable jar
1. Package the project via Maven
    ```
    # In transfer-service directory
    $ mvn clean package -U 
    ```
2. Go to executable jar director
    ```
    # In executable jar director
    $ java -jar transfer-service-1.0.0-SNAPSHOT.jar
    ```
   
## Configurations
* The application has default configurations. 
    * A. For Server configuration:
        * Default host: 0.0.0.0
        * Default port: 8080
    * B. For database it will be running in H2 database. It has sql init script, which runs by the application in every boot-up. The sql script includes DML commands and sample dummy accounts.
    * C. To change the configuration you need to edit the `application.properties` which can be found in `src/main/java/resources`. Here is the list of application configuration that you can change:
        * `server-host` - for http server host, it should be a valid hostname or IP
        * `server-port` - for http server port, it should be unused port
        * `database.url` - for your prefer database url. Example: "jdbc:h2:~/test"
        * `database.init-db` - boolean value if you want to run the init script of not. Init script should be run at least once.
        
## Transfer API
* The transfer API is can be use by sending a http request via this path ``/v1/transfer``
    ##### Sample Request Body
    ```
    {
        "source_acct_id" : 100001,
        "target_acct_id" : 100002,
        "amount" : 550.0,
        "currency" : "PHP"
    }
     ``` 
    * `source_account_id` - should be an existing account in the DB
    * `target_account_id` - should be an existing account in the DB
    * `amount` - amount to be transfer
    * `currency` - amount currency
    
    ##### Sample Successful Response Body
    ```
    {
        "code": "0000",
        "message": "Successful",
        "transaction_id": "190b2440-365a-4604-80e7-dbf9d091d851",
        "updated_balance": {
            "source_updated_balance": 999450.0,
            "target_updated_balance": 1000550.0
        }
    }
    ```
  
    ##### Sample Error Response Body
    ```
    {
        "code": "5100",
        "message": "Insufficient balance",
        "transaction_id": "5e61046f-c9f1-4ba1-abf1-8671fa5f5b87",
        "updated_balance": null
    }
    ```
     * `code` - response code
     * `message` - response message
     * `transactio_id` - id to track the transaction
     * `updated_balance` - updated balances
        * `source_updated_balance` - updated source balance
        * `target_updated_balance` - updated source balance

## Database Schema
* I design a simple schema that will support a simple transfer transaction, but still extendable to support a more complicated transfer transaction e.i transfer with fees and commissions. The schema also has a double entry ledger in the form of the `money_movement` table.
    * Accounts Table:
        ```
        account_id bigint NOT NULL,
        balance double NOT NULL,
        CONSTRAINT accounts_pkey PRIMARY KEY(account_id)
        ``` 
        * Table that holds the account id and its balance
    
    * Money Movement Table
        ```
        transaction_id varchar(36) NOT NULL,
        account_id bigint NOT NULL,
        amount double NOT NULL,
        currency varchar(3) NOT NULL,
        is_credit boolean NOT NULL,
        ```
        * This the double entry ledger representation. This tracks the money movement (credit or debit) of each account.
        * `account_id` - affected account_id
        * `amount` - amount to be credited or debited in the account
        * `currency` - currency of the amount
        * `is_credit` - boolean value if amount is to be credited or debited in account
    
    * Transaction State Table
        ```
        transaction_id varchar(36) NOT NULL,
        initiator_account_id bigint NOT NULL,
        transaction_amount double NOT NULL,
        currency varchar(3) NOT NULL,
        response_code varchar(4) NOT NULL,
        state varchar(10) NOT NULL
        ``` 
        * This holds the state of each transaction received by the system. Transaction is `POSTED` if accepted, else `DECLINE`.
        * `initiator_account_id` - id of the account that initiated the transaction
        * `transaction_amount` - transaction to transfer
        * `currecncy` - currency of the amount
        * `response_code` - response code sent to the requester
        * `state` - state of the transaction. Either `POSTED` or `DECLINE`