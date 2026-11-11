
import "dotenv/config";
import argon2 from "argon2";
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

async function main(): Promise<void> {
  const fullName = process.env.BOOTSTRAP_ADMIN_NAME?.trim();
  const email = process.env.BOOTSTRAP_ADMIN_EMAIL?.trim().toLowerCase();
  const password = process.env.BOOTSTRAP_ADMIN_PASSWORD;
  const confirmation = process.env.BOOTSTRAP_ADMIN_CONFIRM;

  if (confirmation !== "CREATE_FIRST_ADMIN") {
    throw new Error("Bootstrap confirmation is missing.");
  }

  if (!fullName || fullName.length < 2 || fullName.length > 120) {
    throw new Error("Invalid admin name.");
  }

  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    throw new Error("Invalid admin email.");
  }

  if (!password || password.length < 12 || password.length > 128) {
    throw new Error("Password must contain 12 to 128 characters.");
  }

  const passwordHash = await argon2.hash(password, {
    type: argon2.argon2id
  });

  const admin = await prisma.$transaction(async (tx) => {
    await tx.$queryRaw<{ locked: number }[]>`
  SELECT 1::int AS locked
  FROM (SELECT pg_advisory_xact_lock(734921, 1)) AS advisory_lock
`;

    const count = await tx.appUser.count({
      where: { role: "ADMIN" }
    });

    if (count > 0) {
      throw new Error("An ADMIN already exists. Bootstrap refused.");
    }

    const existing = await tx.appUser.findUnique({
      where: { email }
    });

    if (existing) {
      throw new Error("This email already belongs to another account.");
    }

    return tx.appUser.create({
      data: {
        fullName,
        email,
        passwordHash,
        role: "ADMIN",
        status: "ACTIVE"
      },
      select: {
        id: true,
        fullName: true,
        email: true,
        role: true,
        status: true,
        createdAt: true
      }
    });
  });

  console.log("FIRST ADMIN CREATED SUCCESSFULLY");
  console.log(JSON.stringify(admin, null, 2));
}

main()
  .catch((error: unknown) => {
    console.error(
      "BOOTSTRAP FAILED:",
      error instanceof Error ? error.message : error
    );
    process.exitCode = 1;
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
