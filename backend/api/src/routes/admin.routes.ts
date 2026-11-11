
import { Router } from "express";
import {
  listUsers,
  getUserById,
  updateUserStatus
} from "../controllers/admin.controller";
import { authenticate } from "../middleware/auth.middleware";
import { requireRole } from "../middleware/role.middleware";

const router = Router();

// All routes in this file require an authenticated ADMIN.
router.use(authenticate, requireRole("ADMIN"));

router.get("/users", listUsers);
router.get("/users/:id", getUserById);
router.patch("/users/:id/status", updateUserStatus);

export default router;
