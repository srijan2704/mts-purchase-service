# MTS Purchase Service + MTS Finance Dashboard
## AWS EC2 Runbook (Post Git Pull)

Last updated: 2026-03-03

This runbook contains commands to:
- update backend and frontend after `git pull`
- rebuild and restart backend service via `systemd`
- update runtime env safely
- update `systemd` resource limits and restart
- verify health, logs, and network access

---

## 1. Paths and Services

- Backend repo path: `/opt/mts-purchase-service`
- Backend service name: `mts-purchase-service`
- Backend jar path: `/opt/mts-purchase-service/app.jar`
- Backend env file: `/etc/mts-purchase-service.env`
- Backend systemd unit: `/etc/systemd/system/mts-purchase-service.service`
- Frontend repo path: `/opt/mts-finance-dashboard`
- Frontend deploy path: `/var/www/mts-finance-dashboard`
- Nginx config path: `/etc/nginx/conf.d/mts-finance-dashboard.conf`

---

## 2. One-Time Prerequisites (Fresh EC2)

```bash
sudo dnf update -y
sudo dnf install -y git java-21-amazon-corretto-devel nginx rsync
```

Optional but recommended on t2.small (swap):

```bash
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048 status=progress
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
free -h
```

---

## 3. Daily / Post-Pull Backend Update

### 3.1 Pull latest code

```bash
cd /opt/mts-purchase-service
git pull origin main
```

### 3.2 Build jar

```bash
cd /opt/mts-purchase-service
chmod +x mvnw
./mvnw clean package -DskipTests
cp "$(ls target/*.jar | grep -v 'original-' | head -n1)" /opt/mts-purchase-service/app.jar
```

### 3.3 Restart service

```bash
sudo systemctl restart mts-purchase-service
sudo systemctl status mts-purchase-service
```

If status opens pager, press `q` to exit.

---

## 4. Daily / Post-Pull Frontend Update

```bash
cd /opt/mts-finance-dashboard
git pull origin main
sudo rsync -av --delete /opt/mts-finance-dashboard/ /var/www/mts-finance-dashboard/
sudo systemctl restart nginx
sudo systemctl status nginx
```

---

## 5. Backend Start/Stop/Logs Commands

Start service:

```bash
sudo systemctl start mts-purchase-service
```

Enable auto-start on reboot:

```bash
sudo systemctl enable mts-purchase-service
```

Start + enable in one command:

```bash
sudo systemctl enable --now mts-purchase-service
```

Stop service:

```bash
sudo systemctl stop mts-purchase-service
```

Disable auto-start:

```bash
sudo systemctl disable mts-purchase-service
```

Follow logs:

```bash
sudo journalctl -u mts-purchase-service -f
```

Show recent logs:

```bash
sudo journalctl -u mts-purchase-service --since "30 min ago" --no-pager
```

Exit live logs: `Ctrl + C`

---

## 6. Edit Environment Variables

Open env file:

```bash
sudo vi /etc/mts-purchase-service.env
```

Example template:

```env
PROJ_ENVIRONMENT=uat
DB_URL=jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.ap-mumbai-1.oraclecloud.com))(connect_data=(service_name=YOUR_SERVICE_NAME))(security=(ssl_server_dn_match=yes)))
DB_USERNAME=YOUR_DB_USERNAME
DB_PASSWORD=YOUR_DB_PASSWORD

MAIL_USERNAME=bgp.maatarastore@gmail.com
MAIL_PASSWORD=YOUR_GMAIL_APP_PASSWORD
AUTH_REG_OTP_FROM_EMAIL=bgp.maatarastore@gmail.com
AUTH_REG_OTP_OWNER_EMAIL=bgp.maatarastore@gmail.com
```

Apply env changes:

```bash
sudo systemctl daemon-reload
sudo systemctl restart mts-purchase-service
```

Verify active profile from logs:

```bash
sudo journalctl -u mts-purchase-service --no-pager | grep -i "The following 1 profile is active"
```

---

## 7. Edit systemd Unit (CPU/RAM Limits)

Open unit file:

```bash
sudo vi /etc/systemd/system/mts-purchase-service.service
```

Recommended unit snippet:

```ini
[Unit]
Description=MTS Purchase Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/mts-purchase-service
EnvironmentFile=/etc/mts-purchase-service.env
ExecStart=/usr/bin/java -Xms1024m -Xmx1536m -XX:+UseG1GC -jar /opt/mts-purchase-service/app.jar
Restart=always
RestartSec=5
CPUQuota=75%
MemoryHigh=1G
MemoryMax=2G

[Install]
WantedBy=multi-user.target
```

Apply unit changes:

```bash
sudo systemctl daemon-reload
sudo systemctl restart mts-purchase-service
sudo systemctl status mts-purchase-service
```

---

## 8. Network and Access Checks

Local app checks on EC2:

```bash
curl -I http://127.0.0.1:8080/swagger-ui/index.html
curl -I http://127.0.0.1:8080/v3/api-docs
```

Check listening port:

```bash
sudo ss -ltnp | grep 8080
```

Expected external swagger URL (if 8080 exposed):

```text
http://<EC2_PUBLIC_IP>:8080/swagger-ui/index.html
```

If behind nginx reverse proxy:

```text
http://<EC2_PUBLIC_IP>/swagger-ui/index.html
```

---

## 9. AWS Security Group Rules (Minimum)

Inbound rules:
- SSH `22` from your IP only
- HTTP `80` from `0.0.0.0/0` (if frontend via nginx)
- HTTPS `443` from `0.0.0.0/0` (if SSL enabled)
- App port `8080` from your IP (or internal only if proxied)

---

## 10. Common Failures and Fix

### ORA-12506 / ACL filtering
Cause: EC2 outbound IP not in Oracle Autonomous DB allowlist.
Fix:
1. Add EC2 public outbound IP in OCI network ACL / allowed IP list.
2. Restart backend:

```bash
sudo systemctl restart mts-purchase-service
```

### App up but swagger inaccessible
Check:
1. app started logs
2. `ss -ltnp | grep 8080`
3. security group inbound rule on 8080
4. correct URL path with/without context path

### Service restart loop
Stop temporarily:

```bash
sudo systemctl stop mts-purchase-service
```

Investigate logs:

```bash
sudo journalctl -u mts-purchase-service --since "15 min ago" --no-pager
```

---

## 11. Quick Recovery Commands (Copy/Paste)

```bash
cd /opt/mts-purchase-service && git pull origin main && ./mvnw clean package -DskipTests && cp "$(ls target/*.jar | grep -v 'original-' | head -n1)" /opt/mts-purchase-service/app.jar && sudo systemctl restart mts-purchase-service && sudo systemctl status mts-purchase-service
```

```bash
cd /opt/mts-finance-dashboard && git pull origin main && sudo rsync -av --delete /opt/mts-finance-dashboard/ /var/www/mts-finance-dashboard/ && sudo systemctl restart nginx && sudo systemctl status nginx
```

---

## 12. Security Notes

- Do not keep secrets in repo `.env` files.
- Use `/etc/mts-purchase-service.env` with restricted permissions.
- Rotate DB and SMTP credentials if exposed.

Set secure file permissions:

```bash
sudo chown root:root /etc/mts-purchase-service.env
sudo chmod 600 /etc/mts-purchase-service.env
```

