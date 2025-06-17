import os
from dotenv import load_dotenv

# Load environment variables from .env file if it exists
load_dotenv()

# Base configuration
BASE_URL = "http://localhost:8080"  # Force the correct URL
API_PREFIX = "/api"
EXTERNAL_URL = "http://localhost:5001"

# Authentication configuration
ADMIN_EMAIL = os.getenv("ADMIN_EMAIL", "")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "")

# Test user configuration
TEST_USER_EMAIL_PREFIX = os.getenv("TEST_USER_EMAIL_PREFIX", "testuser")
# Force the correct password format for Spring Boot validation
TEST_USER_PASSWORD = "Password123!"  # Override any env variable
TEST_USER_DOMAIN = os.getenv("TEST_USER_DOMAIN", "example.com")

# Load test configuration (reduced for database capacity)
DEFAULT_USERS = int(os.getenv("DEFAULT_USERS", "5"))
DEFAULT_SPAWN_RATE = float(os.getenv("DEFAULT_SPAWN_RATE", "0.5"))
DEFAULT_RUN_TIME = os.getenv("DEFAULT_RUN_TIME", "60s")

# Account configuration for testing
DEFAULT_ACCOUNT_ID = int(os.getenv("DEFAULT_ACCOUNT_ID", "1"))
DEFAULT_TRANSFER_AMOUNT = float(os.getenv("DEFAULT_TRANSFER_AMOUNT", "100.0"))
