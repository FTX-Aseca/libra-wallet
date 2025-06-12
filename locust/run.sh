#!/bin/bash

# Quick setup script for Libra Wallet Load Tests
# This script sets up the Python environment and installs dependencies

set -e  # Exit on any error

echo "🚀 Setting up Libra Wallet Load Tests..."

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.8+ first."
    echo "On macOS: brew install python"
    exit 1
fi

echo "✅ Python 3 found: $(python3 --version)"

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python3 -m venv venv
else
    echo "✅ Virtual environment already exists"
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
source venv/bin/activate

# Upgrade pip
echo "⬆️ Upgrading pip..."
pip install --upgrade pip

# Install dependencies
echo "📥 Installing dependencies..."
pip install -r requirements.txt

# Verify Locust installation
echo "🔍 Verifying Locust installation..."
locust --version

echo ""
echo "🎉 Setup complete! You can now run load tests."
echo ""
echo "Quick start commands:"
echo "  1. Activate the virtual environment: source venv/bin/activate"
echo "  2. Start your API server in the main project directory."
echo "  3. Run load test in Web UI mode: locust -f locustfile.py --host=http://localhost:8080"
echo "  4. Open web UI in your browser: http://localhost:8089"
echo "  5. Run load test in headless mode: locust -f locustfile.py --headless --users 10 --spawn-rate 1 --run-time 60s --host=http://localhost:8080"
echo ""
echo "For more details and customization, see README.md"
