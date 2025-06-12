import os
from dotenv import load_dotenv

# Load environment variables from .env file if it exists
load_dotenv()

# Base configuration
BASE_URL = os.getenv("BASE_URL", "")
API_PREFIX = "/api"

# Authentication configuration
ADMIN_EMAIL = os.getenv("ADMIN_EMAIL", "")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "")

# Test user configuration
TEST_USER_EMAIL_PREFIX = os.getenv("TEST_USER_EMAIL_PREFIX", "testuser")
TEST_USER_PASSWORD = os.getenv("TEST_USER_PASSWORD", "password123")
TEST_USER_DOMAIN = os.getenv("TEST_USER_DOMAIN", "example.com")

# Load test configuration
DEFAULT_USERS = int(os.getenv("DEFAULT_USERS", "10"))
DEFAULT_SPAWN_RATE = float(os.getenv("DEFAULT_SPAWN_RATE", "1"))
DEFAULT_RUN_TIME = os.getenv("DEFAULT_RUN_TIME", "60s")

# Account configuration for testing
DEFAULT_ACCOUNT_ID = int(os.getenv("DEFAULT_ACCOUNT_ID", ""))
DEFAULT_TRANSFER_AMOUNT = float(os.getenv("DEFAULT_TRANSFER_AMOUNT", ""))
