#!/bin/bash

# Navigate to home and create dir
cd ~
mkdir -p cinemana-proxy
cd cinemana-proxy

# Install node dependencies
npm init -y
npm install express http-proxy-middleware cors

# Create server.js
cat << 'EOF' > server.js
const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const cors = require('cors');

const app = express();
app.use(cors());

app.use('/api', createProxyMiddleware({
    target: 'https://cinemana.shabakaty.com',
    changeOrigin: true,
    secure: false,
    onProxyReq: (proxyReq) => {
        proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');
        proxyReq.setHeader('Origin', 'https://cinemana.shabakaty.com');
        proxyReq.setHeader('Referer', 'https://cinemana.shabakaty.com/');
    }
}));

app.listen(3000, () => {
    console.log('Proxy running on port 3000');
});
EOF

# Create tunnel script
cat << 'EOF' > start_tunnel.sh
#!/bin/bash
sshpass -p "Vv2wX%t_22X%" ssh -o ServerAliveInterval=60 -o ServerAliveCountMax=3 -o StrictHostKeyChecking=no -R 8080:localhost:3000 root@158.220.120.204 -N
EOF

chmod +x start_tunnel.sh

# Install pm2
echo "07702098319mustafa" | sudo -S npm install -g pm2

# Start processes
pm2 delete all
pm2 start server.js --name "cinemana-proxy"
pm2 start start_tunnel.sh --name "ssh-tunnel"
pm2 save
