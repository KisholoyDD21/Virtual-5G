from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """
    Every value here is overridable via environment variable of the same
    name (case-insensitive), or a .env file - see .env.example. Nothing
    sensitive has a real default; DATABASE_URL falls back to local SQLite
    so `uvicorn app.main:app` works out of the box for development without
    requiring Postgres to be running.
    """

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_name: str = "Virtual 5G Backend"
    environment: str = "development"

    database_url: str = "sqlite:///./virtual5g.db"
    redis_url: str = "redis://localhost:6379/0"

    jwt_secret_key: str = "dev-secret-change-me"
    jwt_algorithm: str = "HS256"
    jwt_expire_minutes: int = 60 * 24

    # Speed test defaults served by GET /speedtest/config - the Android app
    # can be pointed at a different CDN/region without an app release.
    speedtest_download_url: str = "https://speed.cloudflare.com/__down"
    speedtest_upload_url: str = "https://speed.cloudflare.com/__up"

    telemetry_enabled_by_default: bool = False


@lru_cache
def get_settings() -> Settings:
    return Settings()
