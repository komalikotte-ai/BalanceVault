CREATE UNIQUE INDEX uq_one_pending_account_request_per_applicant
ON account_opening_requests (applicant_user_id)
WHERE status = 'PENDING';