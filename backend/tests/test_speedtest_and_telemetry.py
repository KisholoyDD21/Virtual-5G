def test_speedtest_config_returns_endpoints(client):
    response = client.get("/speedtest/config")
    assert response.status_code == 200
    body = response.json()
    assert body["download_url"].startswith("https://")
    assert body["quick_download_bytes"] < body["standard_download_bytes"] < body["deep_download_bytes"]


def test_speedtest_result_submission_works_anonymously(client):
    response = client.post("/speedtest/results", json={"tier": "quick", "download_mbps": 50.0})
    assert response.status_code == 201
    assert response.json()["tier"] == "quick"


def test_telemetry_rejected_when_not_opted_in(client, auth_headers):
    response = client.post("/telemetry", headers=auth_headers, json={"event_type": "mode_change"})
    assert response.status_code == 403


def test_telemetry_score_out_of_range_is_rejected(client, auth_headers):
    response = client.post(
        "/telemetry", headers=auth_headers, json={"event_type": "mode_change", "network_score": 150}
    )
    assert response.status_code == 422
