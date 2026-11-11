
import argon2 from "argon2";
import { prisma } from "../config/database";
import { createAccessToken } from "../utils/jwt";
import { LoginInput, RegisterInput } from "../validators/auth.schemas";

export async function registerUser(input: RegisterInput) {
  const passwordHash = await argon2.hash(input.password, {
    type: argon2.argon2id
  });

  const user = await prisma.appUser.create({
    data: {
      fullName: input.fullName,
      email: input.email,
      passwordHash,
      role: "ACCOUNT_HOLDER"
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

  return user;
}

export async function loginUser(input: LoginInput) {
  const user = await prisma.appUser.findUnique({
    where: { email: input.email }
  });

  if (!user || user.status !== "ACTIVE") {
    return null;
  }

  const passwordMatches = await argon2.verify(
    user.passwordHash,
    input.password
  );

  if (!passwordMatches) {
    return null;
  }

  const accessToken = createAccessToken(user.id);

  return {
    accessToken,
    user: {
      id: user.id,
      fullName: user.fullName,
      email: user.email,
      role: user.role,
      status: user.status
    }
  };
}
