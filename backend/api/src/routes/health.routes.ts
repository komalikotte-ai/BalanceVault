
import { Router, Request, Response } from "express";
import { prisma } from "../config/database";

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

// Readiness: confirms that PostgreSQL is reachable.
router.get("/ready", async (_req: Request, res: Response) => {
  try {
    await prisma.$queryRaw`SELECT 1`;

    res.status(200).json({
      success: true,
      service: "BalanceVault API",
      status: "ready",
      database: "connected",
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error("Readiness check failed:", error);

    res.status(503).json({
      success: false,
      service: "BalanceVault API",
      status: "not_ready",
      database: "unavailable",
      timestamp: new Date().toISOString()
    });
  }
});

export default router;
