
import { Router } from "express";
import {
  register,
  login,
  logout,
  getCurrentUser
} from "../controllers/auth.controller";
import { authenticate } from "../middleware/auth.middleware";
import {
  loginRateLimiter,
  registrationRateLimiter
} from "../middleware/rate-limit.middleware";

const router = Router();

router.post("/register", registrationRateLimiter, register);
router.post("/login", loginRateLimiter, login);
router.post("/logout", logout);
router.get("/me", authenticate, getCurrentUser);

export default router;
