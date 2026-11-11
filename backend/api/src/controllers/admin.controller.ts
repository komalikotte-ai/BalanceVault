
import { RequestHandler } from "express";
import { UserStatus } from "@prisma/client";
import { z } from "zod";
import { prisma } from "../config/database";

const listUsersSchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  limit: z.coerce.number().int().min(1).max(100).default(20)
});

const userIdSchema = z.string().uuid();

const updateStatusSchema = z.object({
  status: z.nativeEnum(UserStatus)
});

const safeUserSelect = {
  id: true,
  fullName: true,
  email: true,
  role: true,
  status: true,
  createdAt: true,
  updatedAt: true
} as const;

// GET /api/v1/admin/users
export const listUsers: RequestHandler = async (req, res, next) => {
  const parsed = listUsersSchema.safeParse(req.query);

  if (!parsed.success) {
    res.status(400).json({
      success: false,
      message: "Invalid pagination parameters"
    });
    return;
  }

  try {
    const { page, limit } = parsed.data;
    const skip = (page - 1) * limit;

    const [users, total] = await prisma.$transaction([
      prisma.appUser.findMany({
        select: safeUserSelect,
        orderBy: { createdAt: "desc" },
        skip,
        take: limit
      }),
      prisma.appUser.count()
    ]);

    res.status(200).json({
      success: true,
      users,
      pagination: {
        page,
        limit,
        total,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    next(error);
  }
};

// GET /api/v1/admin/users/:id
export const getUserById: RequestHandler = async (req, res, next) => {
  const parsedId = userIdSchema.safeParse(req.params.id);

  if (!parsedId.success) {
    res.status(400).json({
      success: false,
      message: "Invalid user ID"
    });
    return;
  }

  try {
    const user = await prisma.appUser.findUnique({
      where: { id: parsedId.data },
      select: safeUserSelect
    });

    if (!user) {
      res.status(404).json({
        success: false,
        message: "User not found"
      });
      return;
    }

    res.status(200).json({
      success: true,
      user
    });
  } catch (error) {
    next(error);
  }
};

// PATCH /api/v1/admin/users/:id/status
export const updateUserStatus: RequestHandler = async (
  req,
  res,
  next
) => {
  const parsedId = userIdSchema.safeParse(req.params.id);
  const parsedBody = updateStatusSchema.safeParse(req.body);

  if (!parsedId.success || !parsedBody.success) {
    res.status(400).json({
      success: false,
      message: "Invalid user ID or status"
    });
    return;
  }

  if (!req.user) {
    res.status(401).json({
      success: false,
      message: "Authentication required"
    });
    return;
  }

  if (parsedId.data === req.user.id) {
    res.status(400).json({
      success: false,
      message: "You cannot change your own account status"
    });
    return;
  }

  try {
    const result = await prisma.$transaction(async (tx) => {
      const existingUser = await tx.appUser.findUnique({
        where: { id: parsedId.data },
        select: {
          id: true,
          fullName: true,
          email: true,
          role: true,
          status: true
        }
      });

      if (!existingUser) {
        return null;
      }

      if (existingUser.status === parsedBody.data.status) {
        return {
          user: existingUser,
          changed: false
        };
      }

      const updatedUser = await tx.appUser.update({
        where: { id: parsedId.data },
        data: { status: parsedBody.data.status },
        select: safeUserSelect
      });

      await tx.auditEvent.create({
        data: {
          actorUserId: req.user!.id,
          action: "ADMIN_USER_STATUS_UPDATED",
          entityType: "APP_USER",
          entityId: existingUser.id,
          details: {
            previousStatus: existingUser.status,
            newStatus: parsedBody.data.status,
            targetEmail: existingUser.email
          }
        }
      });

      return {
        user: updatedUser,
        changed: true
      };
    });

    if (!result) {
      res.status(404).json({
        success: false,
        message: "User not found"
      });
      return;
    }

    res.status(200).json({
      success: true,
      message: result.changed
        ? "User status updated successfully"
        : "User already has this status",
      user: result.user
    });
  } catch (error) {
    next(error);
  }
};
