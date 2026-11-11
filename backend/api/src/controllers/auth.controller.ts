
import { RequestHandler } from "express";
import { env } from "../config/env";
import { prisma } from "../config/database";
import {
  loginSchema,
  registerSchema
} from "../validators/auth.schemas";
import {
  loginUser,
  registerUser
} from "../services/auth.service";

const cookieOptions = {
  httpOnly: true,
  secure: env.COOKIE_SECURE,
  sameSite: "lax" as const,
  path: "/",
  maxAge: 15 * 60 * 1000
};

// REGISTER
export const register: RequestHandler = async (req, res, next) => {
  const parsed = registerSchema.safeParse(req.body);

  if (!parsed.success) {
    res.status(400).json({
      success: false,
      message: "Invalid registration details",
      errors: parsed.error.flatten().fieldErrors
    });
    return;
  }

  try {
    const user = await registerUser(parsed.data);

    res.status(201).json({
      success: true,
      message: "Account registered successfully",
      user
    });
  } catch (error) {
    if (
      typeof error === "object" &&
      error !== null &&
      "code" in error &&
      error.code === "P2002"
    ) {
      res.status(409).json({
        success: false,
        message: "An account with this email already exists"
      });
      return;
    }

    console.error("[REGISTER] Registration failed:", error);
    next(error);
  }
};

// LOGIN
export const login: RequestHandler = async (req, res, next) => {
  const parsed = loginSchema.safeParse(req.body);

  if (!parsed.success) {
    res.status(400).json({
      success: false,
      message: "Invalid login details",
      errors: parsed.error.flatten().fieldErrors
    });
    return;
  }

  try {
    const result = await loginUser(parsed.data);

    if (!result) {
      res.status(401).json({
        success: false,
        message: "Invalid email or password"
      });
      return;
    }

    res.cookie("bv_access", result.accessToken, cookieOptions);

    res.status(200).json({
      success: true,
      message: "Login successful",
      user: result.user
    });
  } catch (error) {
    console.error("[LOGIN] Login failed:", error);
    next(error);
  }
};

// LOGOUT
export const logout: RequestHandler = (_req, res) => {
  res.clearCookie("bv_access", {
    httpOnly: true,
    secure: env.COOKIE_SECURE,
    sameSite: "lax",
    path: "/"
  });

  res.status(200).json({
    success: true,
    message: "Logged out successfully"
  });
};

// GET CURRENT USER
export const getCurrentUser: RequestHandler = async (req, res, next) => {
  try {
    if (!req.user) {
      res.status(401).json({
        success: false,
        message: "Authentication required"
      });
      return;
    }

    const user = await prisma.appUser.findUnique({
      where: {
        id: req.user.id
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

    if (!user || user.status !== "ACTIVE") {
      res.status(401).json({
        success: false,
        message: "User account is unavailable"
      });
      return;
    }

    res.status(200).json({
      success: true,
      user
    });
  } catch (error) {
    console.error("[AUTH/ME] Failed to fetch current user:", error);
    next(error);
  }
};
