import random
import time
from locust import HttpUser, task, between, SequentialTaskSet
from common.auth import register_user, login_user, get_auth_headers
from common.config import API_PREFIX, DEFAULT_ACCOUNT_ID, DEFAULT_TRANSFER_AMOUNT, DEFAULT_USERS, DEFAULT_SPAWN_RATE, DEFAULT_RUN_TIME, BASE_URL

# Add sequential TaskSet for full flow
class FullFlowTasks(SequentialTaskSet):
    """
    Sequential task set for full flow: register, login, topup, transfer, check history.
    """
    wait_time = between(1, 1)

    @task
    def register(self):
        email, password, register_response = register_user(self.client)
        if not (email and password and register_response):
            self.interrupt()
        self.email = email
        self.password = password
        self.register_response = register_response

    @task
    def login(self):
        self.token = login_user(self.client, self.email, self.password)
        if not self.token:
            self.interrupt()
        self.headers = get_auth_headers(self.token)
        self.account_id = self.register_response.get("id")

    @task
    def topup(self):
        self.topup_amount = round(random.uniform(10.0, DEFAULT_TRANSFER_AMOUNT), 2)
        # Initiate top-up and capture the order ID
        response = self.client.post(
            f"{API_PREFIX}/topup",
            json={"amount": self.topup_amount},
            headers=self.headers
        )
        if response.status_code != 201:
            return
        order_id = response.json().get("id")
        # Complete the top-up by simulating the external callback
        self.client.post(
            f"{API_PREFIX}/topup/callback",
            json={"id": order_id}
        )

    @task
    def check_history(self):
        for _ in range(getattr(self, 'num_transfers', 0)):
            self.client.get(
                f"{API_PREFIX}/accounts/{self.account_id}/transactions",
                headers=self.headers
            )

class LibraWalletUser(HttpUser):
    """
    Locust user class for testing Libra Wallet API endpoints.
    Simulates user behavior including registration, authentication, and account operations.
    """

    host = BASE_URL
    wait_time = between(1, 1)  # Fixed 1s wait for full sequential flow
    tasks = [FullFlowTasks]

# class AuthOnlyUser(HttpUser):
#     """
#     User class focused on testing authentication endpoints only.
#     """

#     host = BASE_URL
#     wait_time = between(0.5, 2)
#     weight = 2  # Lower weight, fewer users of this type

#     @task(1)
#     def register_and_login(self):
#         """Register a new user and immediately login"""
#         email, password, _ = register_user(self.client)
#         if email and password:
#             time.sleep(1)
#             login_user(self.client, email, password)


class ReadOnlyUser(HttpUser):
    """
    User class focused on read-only operations.
    Simulates users who mainly check balances and transaction history.
    """

    host = BASE_URL
    wait_time = between(2, 5)
    weight = 3  # Higher weight, more users of this type

    def on_start(self):
        """Setup authentication and dynamic account ID for read-only user"""
        email, password, register_response = register_user(self.client)
        if register_response and register_response.get("id"):
            self.account_id = register_response.get("id")
        else:
            self.account_id = None
        if email and password:
            time.sleep(1)
            self.token = login_user(self.client, email, password)
        else:
            self.token = None

    def get_headers(self):
        """Get headers with authentication token"""
        if self.token:
            return get_auth_headers(self.token)
        return {}

    @task(5)
    def check_balance(self):
        """Frequently check account balance"""
        if not self.token:
            return

        headers = self.get_headers()
        self.client.get(
            f"{API_PREFIX}/accounts/{self.account_id}/balance",
            headers=headers
        )

    @task(1)
    def check_account_details(self):
        """Occasionally check account details"""
        if not self.token:
            return

        headers = self.get_headers()
        self.client.get(
            f"{API_PREFIX}/accounts/{self.account_id}",
            headers=headers
        )

    @task(2)
    def check_account_transactions(self):
        """Check account-specific transaction history"""
        if not self.token:
            return

        headers = self.get_headers()
        self.client.get(
            f"{API_PREFIX}/accounts/{self.account_id}/transactions",
            headers=headers
        )
