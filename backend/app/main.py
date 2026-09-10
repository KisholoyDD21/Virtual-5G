from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.database import init_db
from app.routers import auth, health, plan, speedtest, telemetry

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    yield


app = FastAPI(
    title=settings.app_name,
    description=(
        "Optional backend services for the Virtual 5G Android app: auth, "
        "plan verification abstraction, speed-test config/results, and "
        "opt-in telemetry aggregation. Virtual 5G does not convert 4G "
        "hardware into real 5G - see /docs and the project README."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # tighten to specific origins before any real deployment
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(auth.router)
app.include_router(plan.router)
app.include_router(speedtest.router)
app.include_router(telemetry.router)
