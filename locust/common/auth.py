import requests
import random
import string
from common.config import BASE_URL, API_PREFIX, TEST_USER_EMAIL_PREFIX, TEST_USER_PASSWORD, TEST_USER_DOMAIN

def generate_test_user_email():
    """Generate a unique test user email"""
    random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
    return f"{TEST_USER_EMAIL_PREFIX}{random_suffix}@{TEST_USER_DOMAIN}"

def generate_register_payload():
    """Generate a registration payload with fake data"""
    return {
        "email": generate_test_user_email(),
        "password": TEST_USER_PASSWORD
    }

def register_user(client):
    """Register a new user and return the response"""
    payload = generate_register_payload()
    
    with client.post(f"{API_PREFIX}/auth/register", json=payload, catch_response=True) as response:
        if response.status_code == 201:
            return payload["email"], payload["password"], response.json()
        elif response.status_code == 400 and "already exists" in response.text:
            # If user already exists, attempt to log in instead
            return payload["email"], payload["password"], None
        else:
            response.failure(f"Registration failed with status {response.status_code}")
            return None, None, None

def login_user(client, email, password):
    """Login with user credentials and return JWT token"""
    payload = {
        "email": email,
        "password": password
    }
    with client.post(f"{API_PREFIX}/auth/login", json=payload, catch_response=True) as response:
        if response.status_code == 200:
            return response.json().get("token")
        else:
            response.failure(f"Login failed for user {email} with status {response.status_code}")
            return None

def get_auth_headers(token):
    """Get authorization headers with JWT token"""
    return {"Authorization": f"Bearer {token}"}
