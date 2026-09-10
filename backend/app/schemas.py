from datetime import datetime

from pydantic import BaseModel, EmailStr, Field


class UserCreate(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8)


class UserRead(BaseModel):
    id: str
    email: EmailStr
    telemetry_opt_in: bool

    model_config = {"from_attributes": True}


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"


class PlanInfoRead(BaseModel):
    carrier_name: str | None = None
    plan_name: str | None = None
    five_g_eligible: bool
    expiry: str | None = None
    verification_source: str

    model_config = {"from_attributes": True}


class UserProvidedPlanCreate(BaseModel):
    carrier_name: str
    plan_name: str
    five_g_eligible: bool
    expiry: str | None = None


class SpeedTestConfig(BaseModel):
    download_url: str
    upload_url: str
    quick_download_bytes: int = 2_000_000
    standard_download_bytes: int = 10_000_000
    deep_download_bytes: int = 25_000_000


class SpeedTestResultCreate(BaseModel):
    tier: str
    download_mbps: float | None = None
    upload_mbps: float | None = None
    ping_ms: float | None = None
    jitter_ms: float | None = None
    packet_loss_percent: float | None = None


class SpeedTestResultRead(SpeedTestResultCreate):
    id: str
    created_at: datetime

    model_config = {"from_attributes": True}


class TelemetryEventCreate(BaseModel):
    event_type: str
    operating_mode: str | None = None
    network_score: int | None = Field(default=None, ge=0, le=100)


class HealthResponse(BaseModel):
    status: str
    environment: str
