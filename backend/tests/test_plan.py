def test_get_plan_returns_mock_carrier_data(client, auth_headers):
    response = client.get("/plan", headers=auth_headers)
    assert response.status_code == 200
    body = response.json()
    assert body["verification_source"] == "carrier_api"
    assert body["five_g_eligible"] is True


def test_manual_plan_is_always_labeled_user_provided(client, auth_headers):
    response = client.post(
        "/plan/manual",
        headers=auth_headers,
        json={"carrier_name": "Acme Mobile", "plan_name": "Basic 4G", "five_g_eligible": False},
    )
    assert response.status_code == 201
    body = response.json()
    assert body["verification_source"] == "user_provided"
    assert body["five_g_eligible"] is False


def test_manual_plan_requires_auth(client):
    response = client.post(
        "/plan/manual",
        json={"carrier_name": "Acme Mobile", "plan_name": "Basic 4G", "five_g_eligible": False},
    )
    assert response.status_code == 401
