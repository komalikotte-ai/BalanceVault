
import Redis from "ioredis";
import { env } from "./env";

export const redis = new Redis(env.REDIS_URL, {
  maxRetriesPerRequest: 1,
  enableOfflineQueue: true,
  retryStrategy(times) {
    return Math.min(times * 250, 2000);
  }
});

redis.on("error", (error: Error) => {
  console.error("Redis connection error:", error.message);
});

export async function connectRedis(): Promise<void> {
  await redis.ping();
  console.log("Redis connected successfully.");
}

export async function disconnectRedis(): Promise<void> {
  if (redis.status === "ready") {
    await redis.quit();
  } else {
    redis.disconnect();
  }
}
