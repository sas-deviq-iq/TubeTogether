cat << 'EOF' > ~/cinemana-proxy/start_tunnel.sh
#!/bin/bash
sshpass -p "07702098319Mustafa@" ssh -o ServerAliveInterval=60 -o ServerAliveCountMax=3 -o StrictHostKeyChecking=no -R 8080:localhost:3000 root@158.220.120.204 -N
EOF
chmod +x ~/cinemana-proxy/start_tunnel.sh
pm2 restart ssh-tunnel
