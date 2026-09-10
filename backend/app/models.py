import uuid
from datetime import datetime, timezone

from sqlalchemy import Boolean, DateTime, Float, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


def _uuid() -> str:
    return str(uuid.uuid4())


def _now() -> datetime:
    return datetime.now(timezone.utc)


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String, primary_key=True, default=_uuid)
    email: Mapped[str] = mapped_column(String, unique=True, index=True)
    hashed_password: Mapped[str] = mapped_column(String)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now)
    telemetry_opt_in: Mapped[bool] = mapped_column(Boolean, default=False)

    plan_verifications: Mapped[list["PlanVerification"]] = relationship(back_populates="user")


class PlanVerification(Base):
    """
    Server-side mirror of the Android CarrierProvider abstraction. In this
    MVP it's always populated from the mock provider or from what the user
    entered manually - never from an unauthorized carrier scrape (see
    docs/limitations.md).
    """

    __tablename__ = "plan_verifications"

    id: Mapped[str] = mapped_column(String, primary_key=True, default=_uuid)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    carrier_name: Mapped[str | None] = mapped_column(String, nullable=True)
    plan_name: Mapped[str | None] = mapped_column(String, nullable=True)
    five_g_eligible: Mapped[bool] = mapped_column(Boolean, default=False)
    expiry: Mapped[str | None] = mapped_column(String, nullable=True)
    verification_source: Mapped[str] = mapped_column(String, default="unverified")
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now)

    user: Mapped["User"] = relationship(back_populates="plan_verifications")


class SpeedTestResult(Base):
    __tablename__ = "speed_test_results"

    id: Mapped[str] = mapped_column(String, primary_key=True, default=_uuid)
    user_id: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    tier: Mapped[str] = mapped_column(String)
    download_mbps: Mapped[float | None] = mapped_column(Float, nullable=True)
    upload_mbps: Mapped[float | None] = mapped_column(Float, nullable=True)
    ping_ms: Mapped[float | None] = mapped_column(Float, nullable=True)
    jitter_ms: Mapped[float | None] = mapped_column(Float, nullable=True)
    packet_loss_percent: Mapped[float | None] = mapped_column(Float, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now)


class TelemetryEvent(Base):
    """
    Only ever written when the submitting user has telemetry_opt_in=True -
    enforced in the router, not just the client (spec section 15: "Give
    users control over telemetry").
    """

    __tablename__ = "telemetry_events"

    id: Mapped[str] = mapped_column(String, primary_key=True, default=_uuid)
    user_id: Mapped[str | None] = mapped_column(ForeignKey("users.id"), nullable=True)
    event_type: Mapped[str] = mapped_column(String)
    operating_mode: Mapped[str | None] = mapped_column(String, nullable=True)
    network_score: Mapped[int | None] = mapped_column(Integer, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_now)
