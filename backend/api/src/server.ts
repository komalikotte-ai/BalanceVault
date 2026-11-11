
import app from "./app";
import { env } from "./config/env";
import {
  connectDatabase,
  disconnectDatabase
} from "./config/database";
import {
  connectRedis,
  disconnectRedis
} from "./config/redis";

async function startServer(): Promise<void> {
  try {
    await connectDatabase();
    await connectRedis();

    const server = app.listen(env.PORT, () => {
      console.log(`BalanceVault API running on port ${env.PORT}`);
      console.log(`Health: http://localhost:${env.PORT}/api/v1/health`);
      console.log(`Ready:  http://localhost:${env.PORT}/api/v1/ready`);
    });

    let shuttingDown = false;

    const shutdown = (signal: string): void => {
      if (shuttingDown) return;
      shuttingDown = true;

      console.log(`${signal} received. Shutting down...`);

      server.close(async () => {
        try {
          await Promise.all([
            disconnectDatabase(),
            disconnectRedis()
          ]);

          console.log("Server, database and Redis connections closed.");
          process.exit(0);
        } catch (error) {
          console.error("Shutdown error:", error);
          process.exit(1);
        }
      });
    };

    process.on("SIGINT", () => shutdown("SIGINT"));
    process.on("SIGTERM", () => shutdown("SIGTERM"));
  } catch (error) {
    console.error("Failed to start BalanceVault API:", error);

    await Promise.allSettled([
      disconnectDatabase(),
      disconnectRedis()
    ]);

    process.exit(1);
  }
}

void startServer();
