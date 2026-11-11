import { NextFunction, Request, Response } from 'express';
import { prisma } from '../config/prisma.js';
import { sendSuccess } from '../utils/response.js';

/** Read-only compliance feed for AUDITOR users. */
export const auditorController = {
  async auditLogs(req: Request, res: Response, next: NextFunction) {
    try {
      const limitValue = Number(req.query.limit ?? 100);
      const take = Number.isFinite(limitValue) ? Math.min(Math.max(Math.trunc(limitValue), 1), 250) : 100;
      const logs = await prisma.auditLog.findMany({
        orderBy: { createdAt: 'desc' },
        take,
        select: {
          id: true,
          action: true,
          actorId: true,
          actorRole: true,
          targetResource: true,
          targetId: true,
          metadata: true,
          createdAt: true,
          actor: { select: { fullName: true, email: true } },
        },
      });
      return sendSuccess(res, logs, 'Audit events retrieved successfully');
    } catch (error) {
      return next(error);
    }
  },
};
