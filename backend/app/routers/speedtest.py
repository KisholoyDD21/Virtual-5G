from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.config import get_settings
from app.database import get_db
from app.deps import get_optional_user
from app.models import SpeedTestResult as SpeedTestResultModel
from app.models import User
from app.schemas import SpeedTestConfig, SpeedTestResultCreate, SpeedTestResultRead

router = APIRouter(prefix="/speedtest", tags=["speedtest"])


@router.get("/config", response_model=SpeedTestConfig)
def get_speedtest_config() -> SpeedTestConfig:
    """
    Lets the fleet be pointed at different test infrastructure without an
    app release - the Android SpeedTestEndpoints data class has the same
    shape and can be populated from this response.
    """
    settings = get_settings()
    return SpeedTestConfig(
        download_url=settings.speedtest_download_url,
        upload_url=settings.speedtest_upload_url,
    )


@router.post("/results", response_model=SpeedTestResultRead, status_code=201)
def submit_result(
    payload: SpeedTestResultCreate,
    user: User | None = Depends(get_optional_user),
    db: Session = Depends(get_db),
) -> SpeedTestResultModel:
    """Submission works anonymously too - an account isn't required to run or log a local test."""
    record = SpeedTestResultModel(user_id=user.id if user else None, **payload.model_dump())
    db.add(record)
    db.commit()
    db.refresh(record)
    return record
