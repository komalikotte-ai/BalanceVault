
import app from "./app";
import { env } from "./config/env";
import {
  connectDatabase,
  disconnectDatabase
} from "./config/database";

async function startServer(): Promise<void> {
  try {
    await connectDatabase();

    const server = app.listen(env.PORT, () => {
      console.log(`BalanceVault API running on port ${env.PORT}`);
      console.log(`Health: http://localhost:${env.PORT}/api/v1/health`);
      console.log(`Ready:  http://localhost:${env.PORT}/api/v1/ready`);
    });

    const shutdown = (signal: string): void => {
      console.log(`${signal} received. Shutting down...`);

      server.close(async () => {
        await disconnectDatabase();
        console.log("Server and database connection closed.");
        process.exit(0);
      });
    };

    process.on("SIGINT", () => shutdown("SIGINT"));
    process.on("SIGTERM", () => shutdown("SIGTERM"));
  } catch (error) {
    console.error("Failed to start BalanceVault API:", error);
    await disconnectDatabase();
    process.exit(1);
  }
}

void startServer();
