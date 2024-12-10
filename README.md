# VerveProject

The Application is a Java-based RESTful service designed to handle requests via the /api/verve/accept endpoint. It leverages asynchronous HTTP requests and Redis for ID management and expiry control, providing efficient request handling and logging.

### Approach
- The application receives a _GET_ request with endpoint (optional) and id as query parameters.
- It checks if the id is present in Redis. If not, it increments the counter and stores the id in Redis with an expiry set to the end of the current minute.
- It then sends an asynchronous _HTTP POST_ request to the endpoint with the unique ID count for that minute.
- A thread runs at the start of each minute to reset the unique ID count for the new minute and logs the count for the previous minute.
- Access/operation to the counter is synchronized to ensure thread safety.
- A sliding window approach is used to manage the unique ID count for each minute.
- To prevent id deduplication when service is behind load balancer, the application relies on Redis to manage unique IDs and their expiry.
- To handle a high volume of requests, the application uses an _ExecutorService_ with a _ThreadPoolExecutor_ to manage a pool of threads, which dynamically adjusts the number of threads based on the load.

### _Key Components_

- **Executor Service:** Utilizes a ThreadPoolExecutor to manage a pool of threads, allowing concurrent request processing.
- **HTTP Client**: Configured to use HTTP/2 for asynchronous communication with external endpoints.
- **Redis Integration**: Manages unique IDs with expiry to ensure efficient data handling and prevent de-duplication.
- **Logging**: Logs responses and errors for monitoring and troubleshooting.

### _Core Functionalities_

- **Request Handling:** Accepts GET requests with endpoint and id as query parameters.
If the endpoint is valid, sends an asynchronous HTTP POST request with the unique ID count for that minute.
- **ID Management:** Stores IDs in Redis with a Unix timestamp expiry.
Increments a counter for each new ID, ensuring uniqueness.
- **Response Management:** Returns preliminary responses immediately.
Handles and logs exceptions to maintain service reliability.
- **Minute Unique Count Management:** Manages unique ID count for each minute and resets it at the start of a new minute.

### _How to Use_

- Deploy the service in a Java runtime environment.
- Make GET requests to /api/verve/accept with appropriate query parameters.
- Monitor logs (verve.log file generated) for request processing details and potential errors.

### _Configuration_

- Ensure Redis server is configured and accessible for ID management.
- Following properties can be configured in `verve.properties` file:
  - `verve.base.uri`: Base URI for the service. Default is `http://localhost:8090/`
  - `redis.host`: Redis server host. Default is `localhost`
  - `redis.port`: Redis server port. Default is `6379`
  - `threadpool.size.min`: Minimum thread pool size for executor service of Grizzly server. Default is `100`
  - `threadpool.size.max`: Maximum thread pool size for executor service of Grizzly server. Default is `10000`

### _Dependencies_

- Jakarta RESTful Web Services
- Grizzly HTTP Server
- Java HTTP Client (Java 11+)
- Jersey Dependency Injection
- Redis Client (Jedis)