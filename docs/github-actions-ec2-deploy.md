# GitHub Actions Deployment to EC2

This document configures automatic deployment for:
- Backend: `mts-purchase-service` (`systemd` service: `mts-purchase-service`)
- Frontend: `mts-finance-dashboard` (served by `nginx`)

Workflow file:
- `.github/workflows/deploy-ec2.yml`

EC2 deploy script:
- `scripts/ec2-deploy.sh`

## 1. Required GitHub Secrets

Add these in GitHub repository settings:
`Settings -> Secrets and variables -> Actions -> New repository secret`

- `EC2_HOST`
  - Example: `13.234.56.78`
- `EC2_USER`
  - Example: `ec2-user`
- `EC2_SSH_PRIVATE_KEY`
  - Private key content (PEM) that can SSH into EC2 as `EC2_USER`
- `EC2_KNOWN_HOSTS`
  - Output from:
    - `ssh-keyscan -H <EC2_HOST>`

## 2. EC2 Prerequisites

The workflow assumes:
- Backend repo path exists: `/opt/mts-purchase-service`
- Frontend repo path exists: `/opt/mts-finance-dashboard`
- Backend service exists: `mts-purchase-service`
- Frontend deploy path exists: `/var/www/mts-finance-dashboard`
- `ec2-user` can run:
  - `sudo systemctl restart mts-purchase-service`
  - `sudo systemctl restart nginx`
  - `sudo rsync ... /var/www/mts-finance-dashboard/`

If passwordless `sudo` is not set for deploy commands, deployment will fail.

## 3. Trigger Behavior

- On every push to `main`:
  - deploys backend by default
- Manual run (`workflow_dispatch`):
  - choose backend and/or frontend deployment
  - optionally enable backend tests before packaging

## 4. First Run Checklist

1. Push these workflow changes to `main`.
2. Open `Actions -> Deploy To EC2`.
3. Run manual workflow with:
   - `deploy_backend=true`
   - `deploy_frontend=true`
   - `run_tests=false` (faster first validation)
4. Validate on EC2:
   - `sudo systemctl status mts-purchase-service`
   - `sudo systemctl status nginx`
   - `curl -I http://127.0.0.1:8080/swagger-ui/index.html`
   - open `http://<EC2_PUBLIC_IP>/` and backend API URL

## 5. Notes

- Frontend auto-deploy on frontend repo push requires a similar workflow in the `mts-finance-dashboard` repository.
- Backend health check uses:
  - `http://127.0.0.1:8080/actuator/health`
- Fallback health check (if actuator is restricted):
  - `http://127.0.0.1:8080/swagger-ui/index.html`
- To customize paths or service names on EC2, set environment variables before running `scripts/ec2-deploy.sh`.
