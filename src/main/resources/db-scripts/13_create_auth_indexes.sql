-- Indexes used in login/session validation paths.
CREATE INDEX idx_app_users_username_lower ON app_users (LOWER(username));
CREATE INDEX idx_user_sessions_expires_at ON user_sessions (expires_at);
CREATE INDEX idx_user_sessions_user_active ON user_sessions (user_id, is_revoked);
