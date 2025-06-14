from flask import Flask, request, jsonify

# Seed in-memory CVU accounts with a million entries, each with a huge balance and 22-digit CVUs
PREFIX = '999999999'
SUFFIX_LENGTH = 22 - len(PREFIX)
accounts = {
    f"{PREFIX}{str(i).zfill(SUFFIX_LENGTH)}": 99999999999
    for i in range(1_000_000)
}

app = Flask(__name__)

@app.route("/api/debin", methods=["POST"])
def debin():
    data = request.get_json() or {}
    identifier_type = data.get("identifier_type")
    identifier = data.get("identifier")
    amount = data.get("amount")

    if identifier_type != "cvu":
        return jsonify({"error": "identifier_type must be 'cvu'"}), 400
    if not identifier:
        return jsonify({"error": "identifier is required"}), 400
    if amount is None:
        return jsonify({"error": "amount is required"}), 400

    # Check CVU existence
    if identifier not in accounts:
        return jsonify({"error": "CVU not found"}), 400
    # Check balance
    if accounts[identifier] < amount:
        return jsonify({"error": "Insufficient balance"}), 400
    # Perform debit
    accounts[identifier] -= amount

    return jsonify({
        "identifier": identifier,
        "amount": amount,
    }), 200

@app.route("/api/topup", methods=["POST"])
def topup():
    data = request.get_json() or {}
    identifier_type = data.get("identifier_type")
    identifier = data.get("identifier")
    amount = data.get("amount")

    if identifier_type not in ("alias", "cvu"):
        return jsonify({"error": "identifier_type must be 'alias' or 'cvu'"}), 400
    if not identifier:
        return jsonify({"error": "identifier is required"}), 400
    if amount is None:
        return jsonify({"error": "amount is required"}), 400

    # Check CVU existence
    if identifier not in accounts:
        return jsonify({"error": "CVU not found"}), 400
    # Check balance
    if accounts[identifier] < amount:
        return jsonify({"error": "Insufficient balance"}), 400
    # Perform top-up
    accounts[identifier] += amount
    return jsonify({
        "identifier": identifier,
        "amount": amount,
    }), 200

if __name__ == "__main__":
    # Run the Flask development server
    app.run(debug=True, host="0.0.0.0", port=5001)
