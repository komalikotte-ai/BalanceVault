
import express, { ErrorRequestHandler } from "express";
import cors from "cors";
import helmet from "helmet";
import cookieParser from "cookie-parser";

import { env } from "./config/env";
import healthRoutes from "./routes/health.routes";
import authRoutes from "./routes/auth.routes";
import adminRoutes from "./routes/admin.routes";
import { apiRateLimiter } from "./middleware/rate-limit.middleware";

const app = express();

app.disable("x-powered-by");

app.use(helmet());

app.use(
  cors({
    origin: env.CORS_ORIGIN,
    credentials: true
  })
);

app.use(express.json({ limit: "1mb" }));
app.use(express.urlencoded({ extended: true, limit: "1mb" }));
app.use(cookieParser());

// Shared Redis-backed API rate limit.
app.use("/api/v1", apiRateLimiter);

// API routes
app.use("/api/v1", healthRoutes);
app.use("/api/v1/auth", authRoutes);
app.use("/api/v1/admin", adminRoutes);

// Unknown route handler
app.use((_req, res) => {
  res.status(404).json({
    success: false,
    message: "Route not found"
  });
});

// Central error handler
const errorHandler: ErrorRequestHandler = (
  error,
  _req,
  res,
  _next
) => {
  console.error("Unhandled application error:", error);

  res.status(500).json({
    success: false,
    message: "Internal server error"
  });
};

app.use(errorHandler);

export default app;
