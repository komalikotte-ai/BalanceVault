
import { rateLimit } from "express-rate-limit";
import { RedisStore } from "rate-limit-redis";
import type { RedisReply } from "rate-limit-redis";
import { redis } from "../config/redis";

function createRedisStore(prefix: string): RedisStore {
  return new RedisStore({
    prefix,
    sendCommand: (...args: string[]) =>
      redis.call(args[0], ...args.slice(1)) as Promise<RedisReply>
  });
}

export const apiRateLimiter = rateLimit({
  windowMs: 60 * 1000,
  limit: 300,
  standardHeaders: "draft-8",
  legacyHeaders: false,
  store: createRedisStore("bv:rl:api:"),
  handler: (_req, res) => {
    res.status(429).json({
      success: false,
      message: "Too many API requests. Please try again later."
    });
  }
});

export const loginRateLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 10,
  standardHeaders: "draft-8",
  legacyHeaders: false,
  store: createRedisStore("bv:rl:login:"),
  handler: (_req, res) => {
    res.status(429).json({
      success: false,
      message: "Too many login attempts. Please try again after 15 minutes."
    });
  }
});

export const registrationRateLimiter = rateLimit({
  windowMs: 60 * 60 * 1000,
  limit: 5,
  standardHeaders: "draft-8",
  legacyHeaders: false,
  store: createRedisStore("bv:rl:register:"),
  handler: (_req, res) => {
    res.status(429).json({
      success: false,
      message: "Too many registration attempts. Please try again later."
    });
  }
});
