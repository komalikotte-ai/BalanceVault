
import { RequestHandler } from "express";
import { prisma } from "../config/database";
import { verifyAccessToken } from "../utils/jwt";

export const authenticate: RequestHandler = async (req, res, next) => {
  const token = req.cookies?.bv_access;

  if (!token || typeof token !== "string") {
    res.status(401).json({
      success: false,
      message: "Authentication required"
    });
    return;
  }

  let userId: string;

  try {
    userId = verifyAccessToken(token);
  } catch {
    res.status(401).json({
      success: false,
      message: "Invalid or expired authentication token"
    });
    return;
  }

  try {
    const user = await prisma.appUser.findUnique({
      where: { id: userId },
      select: {
        id: true,
        role: true,
        status: true
      }
    });

    if (!user || user.status !== "ACTIVE") {
      res.status(401).json({
        success: false,
        message: "User account is unavailable"
      });
      return;
    }

    req.user = {
      id: user.id,
      role: user.role
    };

    next();
  } catch (error) {
    next(error);
  }
};
