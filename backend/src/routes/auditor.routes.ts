import { Router } from 'express';
import { Role } from '@prisma/client';
import { auditorController } from '../controllers/auditor.controller.js';
import { authenticate, requireRole } from '../middleware/auth.js';

export const auditorRouter = Router();
auditorRouter.get('/audit-logs', authenticate, requireRole(Role.AUDITOR, Role.ADMIN), auditorController.auditLogs);
