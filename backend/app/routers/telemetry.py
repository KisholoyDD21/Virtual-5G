from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.deps import get_current_user
from app.models import TelemetryEvent, User
from app.schemas import TelemetryEventCreate

router = APIRouter(prefix="/telemetry", tags=["telemetry"])


@router.post("", status_code=status.HTTP_201_CREATED)
def submit_telemetry(
    payload: TelemetryEventCreate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> dict:
    """
    Rejects the write outright if the account hasn't opted in - telemetry
    consent is enforced here, not just trusted from the client (spec
    section 15: "Give users control over telemetry").
    """
    if not user.telemetry_opt_in:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Telemetry is not enabled for this account",
        )
    event = TelemetryEvent(
        user_id=user.id,
        event_type=payload.event_type,
        operating_mode=payload.operating_mode,
        network_score=payload.network_score,
    )
    db.add(event)
    db.commit()
    return {"accepted": True}
