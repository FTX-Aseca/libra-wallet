# Libra Wallet Load Tests

This directory contains Locust-based load tests for the Libra Wallet API. The tests are designed to stress test various endpoints including authentication, account management, transactions, and transfers.

## Prerequisites

- Python 3.8+ installed on your system.
- Your Libra Wallet API server must be running.

## Setup

You can use the provided setup script for a quick start, or follow the manual installation steps.

### Quick Setup (Recommended)

Run the setup script to create a virtual environment and install dependencies:

```bash
./run.sh
```

This will guide you through the setup process.

### Manual Setup

If you prefer to set up the environment manually:

1.  **Create and Activate Virtual Environment:**

    ```bash
    cd locust
    python3 -m venv venv
    source venv/bin/activate  # On macOS/Linux
    # venv\Scripts\activate   # On Windows
    ```

2.  **Install Dependencies:**

    ```bash
    pip install -r requirements.txt
    ```

### Configuration

Test parameters can be configured using environment variables. Create a `.env` file in the `locust` directory to override default settings from `common/config.py`.

1.  **Create a `.env` file:**

    ```bash
    touch .env
    ```

2.  **Add your custom configuration.** Here is an example:

    ```env
    # .env
    BASE_URL=http://localhost:8080

    # Test user configuration
    TEST_USER_EMAIL_PREFIX=testuser
    TEST_USER_PASSWORD=password123
    TEST_USER_DOMAIN=example.com

    # Default locust run parameters
    DEFAULT_USERS=20
    DEFAULT_SPAWN_RATE=2
    DEFAULT_RUN_TIME=120s

    # Account for transfers
    DEFAULT_ACCOUNT_ID=1
    DEFAULT_TRANSFER_AMOUNT=50.0
    ```

## How It Works

The load testing setup consists of:
- `locustfile.py`: Contains the main test scenarios and user behaviors.
- `common/`: A package with shared modules.
  - `config.py`: Manages configuration from environment variables or a `.env` file.
  - `auth.py`: Provides helper functions for user registration and authentication.
- `run.sh`: A script to automate the setup process.

### Test Scenarios in `locustfile.py`

The `locustfile.py` defines different user roles to simulate various types of API traffic. Each role runs a different set of tasks.

-   **`LibraWalletUser`** (Weight: 1)
    This user simulates a complete end-to-end flow. It executes the following tasks in sequence:
    1.  **Register**: Creates a new user account.
    2.  **Login**: Authenticates and obtains a JWT token.
    3.  **Top-up**: Adds a random amount of money to the account.
    4.  **Transfer**: Performs multiple transfers to a default account. The number of transfers is based on the top-up amount.
    5.  **Check History**: Fetches the transaction history.

-   **`ReadOnlyUser`** (Weight: 3)
    This user focuses on read-heavy operations, simulating users who primarily browse information. After registering and logging in, it repeatedly performs:
    -   Checking account balance.
    -   Viewing transaction history.
    -   Fetching account details.

-   **`AuthOnlyUser`** (Weight: 2)
    This user is designed to stress the authentication system by repeatedly:
    -   Registering a new user.
    -   Logging in with the new credentials.

## Running Load Tests

Make sure your Libra Wallet API is running before starting the tests.

### Web UI Mode

This mode provides a web interface to control the load test.

```bash
locust -f locustfile.py --host=http://localhost:8080
```

Open your browser to [http://localhost:8089](http://localhost:8089) to start the test.

-   **Host**: Set this to your API's base URL (e.g., `http://localhost:8080`).

### Headless Mode (Command Line)

For automated runs, you can run Locust in headless mode without the web UI.

```bash
locust -f locustfile.py --headless --users 10 --spawn-rate 1 --run-time 60s --host=http://localhost:8080
```

-   `--users`: Total number of users to simulate.
-   `--spawn-rate`: Number of users to spawn per second.
-   `--run-time`: Duration of the test (e.g., `60s`, `1m30s`, `1h`).

## Understanding Results

### Key Metrics to Monitor

1.  **Requests per Second (RPS)**: How many requests your API handles.
2.  **Response Time**: Average, min, max, and percentile response times.
3.  **Failure Rate**: Percentage of failed requests.
4.  **Active Users**: Number of simulated users.

### Interpreting Performance

-   **Good Performance**: Response times stay low as user count increases.
-   **Bottleneck Detected**: Response times increase dramatically with more users.
-   **System Limits**: RPS plateaus even with more users being added.

## Customization

### Adding New Test Scenarios

1.  Create a new `HttpUser` class in `locustfile.py`:

    ```python
    class MyCustomUser(HttpUser):
        wait_time = between(1, 3)

        @task
        def my_task(self):
            # Your test logic here
            # For example, call a new endpoint
            self.client.get("/api/my-endpoint")
    ```
2. Run locust with your new user. You may want to run only that user by using `locust -f locustfile.py MyCustomUser`.


## Common Issues and Troubleshooting

### 1. Connection Refused
-   Ensure your API server is running.
-   Check the `BASE_URL` in your `.env` file or environment variables.

### 2. Authentication Failures
-   Verify that the API endpoints in `common/auth.py` and `locustfile.py` match your application's routes.
-   Check if user registration is working correctly through the API.

### 3. High Failure Rates
-   Start with a small number of users (e.g., 5-10) and increase gradually.
-   Check your API server logs for errors.
-   Verify that your test data and endpoints are correct.

## Advanced Usage

### Distributed Load Testing

Run tests across multiple machines for large-scale tests:

```bash
# Master machine
locust -f locustfile.py --master --host=http://your-api-server.com

# Worker machines
locust -f locustfile.py --worker --master-host=<master-ip>
```

### CSV Reports

Generate CSV reports for test results:

```bash
locust -f locustfile.py --headless --users 50 --spawn-rate 2 --run-time 300s --csv=results --host=http://localhost:8080
```

This will create `results_stats.csv`, `results_stats_history.csv`, and `results_failures.csv`.



`locust -f locustfile.py --host=http://localhost:8080`
` locust -f locustfile.py --headless --users 10 --spawn-rate 2 --run-time 60s --host http://localhost:8080`