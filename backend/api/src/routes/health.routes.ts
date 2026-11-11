
import { Router, Request, Response } from "express";
import { prisma } from "../config/database";
import { redis } from "../config/redis";

const router = Router();

// Liveness: confirms that the API process is running.
router.get("/health", (_req: Request, res: Response) => {
  res.status(200).json({
    success: true,
    service: "BalanceVault API",
    status: "healthy",
    timestamp: new Date().toISOString()
  });
});

// Readiness: confirms PostgreSQL and Redis are reachable.
router.get("/ready", async (_req: Request, res: Response) => {
  let databaseStatus = "unavailable";
  let redisStatus = "unavailable";

  try {
    await prisma.$queryRaw`SELECT 1`;
    databaseStatus = "connected";

    await redis.ping();
    redisStatus = "connected";

    res.status(200).json({
      success: true,
      service: "BalanceVault API",
      status: "ready",
      database: databaseStatus,
      redis: redisStatus,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error("Readiness check failed:", error);

    res.status(503).json({
      success: false,
      service: "BalanceVault API",
      status: "not_ready",
      database: databaseStatus,
      redis: redisStatus,
      timestamp: new Date().toISOString()
    });
  }
});

export default router;
