import requests
import random
import string
from common.config import BASE_URL, API_PREFIX, TEST_USER_EMAIL_PREFIX, TEST_USER_PASSWORD, TEST_USER_DOMAIN

def generate_test_user_email():
    """Generate a unique test user email with timestamp to ensure uniqueness"""
    import time
    random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
    timestamp = str(int(time.time() * 1000))[-8:]  # Last 8 digits of timestamp
    return f"{TEST_USER_EMAIL_PREFIX}{random_suffix}{timestamp}@{TEST_USER_DOMAIN}"

def generate_register_payload():
    """Generate a registration payload with fake data"""
    return {
        "email": generate_test_user_email(),
        "password": TEST_USER_PASSWORD
    }

def register_user(client):
    """Register a new user and return the response"""
    max_retries = 3
    
    for attempt in range(max_retries):
        payload = generate_register_payload()
        
        with client.post(f"{API_PREFIX}/auth/register", json=payload, catch_response=True) as response:
            if response.status_code == 201:
                return payload["email"], payload["password"], response.json()
            elif response.status_code == 400 and "already exists" in response.text:
                # If user already exists, try generating a new email
                if attempt < max_retries - 1:
                    continue  # Try again with a new email
                else:
                    # On last attempt, fail gracefully
                    response.failure(f"Failed to register unique user after {max_retries} attempts")
                    return None, None, None
            else:
                error_msg = f"Registration failed with status {response.status_code}\n"
                error_msg += f"Request: POST {API_PREFIX}/auth/register\n"
                error_msg += f"Request payload: {payload}\n"
                error_msg += f"Response status: {response.status_code}\n"
                error_msg += f"Response headers: {dict(response.headers)}\n"
                error_msg += f"Response body: {response.text}\n"
                response.failure(error_msg)
                return None, None, None
    
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
            error_msg = f"Login failed for user {email} with status {response.status_code}\n"
            error_msg += f"Request: POST {API_PREFIX}/auth/login\n"
            error_msg += f"Request payload: {payload}\n"
            error_msg += f"Response status: {response.status_code}\n"
            error_msg += f"Response headers: {dict(response.headers)}\n"
            error_msg += f"Response body: {response.text}\n"
            response.failure(error_msg)
            return None

def get_auth_headers(token):
    """Get authorization headers with JWT token"""
    return {"Authorization": f"Bearer {token}"}
