def test_register_then_login_succeeds(client):
    register = client.post("/auth/register", json={"email": "alice@example.com", "password": "password123"})
    assert register.status_code == 201
    assert register.json()["email"] == "alice@example.com"

    login = client.post("/auth/login", data={"username": "alice@example.com", "password": "password123"})
    assert login.status_code == 200
    assert "access_token" in login.json()


def test_duplicate_registration_is_rejected(client):
    client.post("/auth/register", json={"email": "bob@example.com", "password": "password123"})
    second = client.post("/auth/register", json={"email": "bob@example.com", "password": "password123"})
    assert second.status_code == 409


def test_login_with_wrong_password_is_rejected(client):
    client.post("/auth/register", json={"email": "carol@example.com", "password": "password123"})
    response = client.post("/auth/login", data={"username": "carol@example.com", "password": "wrong-password"})
    assert response.status_code == 401


def test_protected_route_requires_token(client):
    response = client.get("/plan")
    assert response.status_code == 401
