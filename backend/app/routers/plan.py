from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.deps import get_current_user
from app.models import PlanVerification, User
from app.providers.carrier_provider import CarrierProvider, get_carrier_provider
from app.schemas import PlanInfoRead, UserProvidedPlanCreate

router = APIRouter(prefix="/plan", tags=["plan"])


@router.get("", response_model=PlanInfoRead)
def get_plan(
    user: User = Depends(get_current_user),
    carrier_provider: CarrierProvider = Depends(get_carrier_provider),
) -> PlanInfoRead:
    """Carrier-sourced plan info (mock in this MVP - see docs/limitations.md)."""
    return carrier_provider.get_plan_info(user.id)


@router.post("/manual", response_model=PlanInfoRead, status_code=201)
def submit_manual_plan(
    payload: UserProvidedPlanCreate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> PlanInfoRead:
    """
    Always stored as verification_source="user_provided" - never upgraded
    to carrier-verified status server-side, matching the Android client's
    SubmitUserProvidedPlanUseCase (spec section 4: "Clearly label
    unverified user-entered information").
    """
    record = PlanVerification(
        user_id=user.id,
        carrier_name=payload.carrier_name,
        plan_name=payload.plan_name,
        five_g_eligible=payload.five_g_eligible,
        expiry=payload.expiry,
        verification_source="user_provided",
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    return PlanInfoRead.model_validate(record)
