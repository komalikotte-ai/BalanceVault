
import jwt, { SignOptions, JwtPayload } from "jsonwebtoken";
import { env } from "../config/env";

export function createAccessToken(userId: string): string {
  return jwt.sign(
    {},
    env.JWT_SECRET,
    {
      subject: userId,
      expiresIn: env.JWT_EXPIRES_IN as SignOptions["expiresIn"]
    }
  );
}

export function verifyAccessToken(token: string): string {
  const decoded = jwt.verify(token, env.JWT_SECRET);

  if (
    typeof decoded === "string" ||
    typeof (decoded as JwtPayload).sub !== "string"
  ) {
    throw new Error("Invalid authentication token");
  }

  return (decoded as JwtPayload).sub as string;
}
