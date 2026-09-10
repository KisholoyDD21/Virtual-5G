from abc import ABC, abstractmethod

from app.schemas import PlanInfoRead


class CarrierProvider(ABC):
    """
    Server-side mirror of the exact same abstraction defined on the Android
    side (domain/repository/CarrierProvider.kt). A real integration
    implements this interface against an actual carrier API and is swapped
    in via dependency injection - nothing else in the backend changes. No
    carrier website is scraped anywhere in this project.
    """

    @abstractmethod
    def get_plan_info(self, user_id: str) -> PlanInfoRead: ...

    @abstractmethod
    def is_5g_eligible(self, user_id: str) -> bool: ...


class MockCarrierProvider(CarrierProvider):
    """Same fixture shape as the Android MockCarrierProvider, for parity between platforms."""

    def get_plan_info(self, user_id: str) -> PlanInfoRead:
        return PlanInfoRead(
            carrier_name="Example Telecom",
            plan_name="Unlimited 5G",
            five_g_eligible=True,
            expiry="2026-10-12",
            verification_source="carrier_api",
        )

    def is_5g_eligible(self, user_id: str) -> bool:
        return self.get_plan_info(user_id).five_g_eligible


def get_carrier_provider() -> CarrierProvider:
    return MockCarrierProvider()
