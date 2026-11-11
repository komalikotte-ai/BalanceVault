
import "dotenv/config";
import { z } from "zod";

const envSchema = z.object({
  NODE_ENV: z
    .enum(["development", "test", "production"])
    .default("development"),

  PORT: z.coerce.number().int().min(1).max(65535).default(5000),

  CORS_ORIGIN: z.string().url().default("http://localhost:5173"),

  JWT_SECRET: z.string().min(32),

  JWT_EXPIRES_IN: z.string().default("15m"),

  REDIS_URL: z.string().url(),

  COOKIE_SECURE: z
    .enum(["true", "false"])
    .default("false")
    .transform((value) => value === "true")
});

export const env = envSchema.parse(process.env);
