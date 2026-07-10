@echo off
title Local Cinemana Proxy & SSH Tunnel
echo ==============================================
echo Starting Local Node.js Proxy...
echo ==============================================
start "Node Proxy" cmd /c "node server.js"

echo Waiting 3 seconds for the server to start...
timeout /t 3 /nobreak > nul

echo ==============================================
echo Connecting Secure SSH Tunnel to VPS...
echo (Port 8080 on VPS --^> Port 3000 on this PC)
echo ==============================================
ssh -R 8080:localhost:3000 root@158.220.120.204
