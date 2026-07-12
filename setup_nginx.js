const { Client } = require('ssh2');
const fs = require('fs');

const sshConfig = {
    host: '158.220.120.204',
    port: 22,
    username: 'root',
    password: '07702098319Mustafa@' // Using the password provided by user
};

const nginxConfig = `
server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Websocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
`;

const conn = new Client();

console.log('Connecting to VPS...');

conn.on('ready', () => {
    console.log('Client :: ready');
    
    // Commands to run
    const commands = [
        'apt-get update',
        'apt-get install -y nginx',
        `echo "${nginxConfig.replace(/\$/g, '\\$').replace(/"/g, '\\"')}" > /etc/nginx/sites-available/proxy`,
        'ln -sf /etc/nginx/sites-available/proxy /etc/nginx/sites-enabled/',
        'rm -f /etc/nginx/sites-enabled/default',
        'systemctl restart nginx',
        'echo "GatewayPorts clientspecified" >> /etc/ssh/sshd_config',
        'systemctl restart ssh',
        'ufw allow 80/tcp',
        'ufw allow 8080/tcp'
    ];
    
    const execNext = (i) => {
        if (i >= commands.length) {
            console.log('All VPS configuration commands executed successfully!');
            conn.end();
            return;
        }
        
        console.log(`Executing: ${commands[i]}`);
        conn.exec(commands[i], (err, stream) => {
            if (err) throw err;
            stream.on('close', (code, signal) => {
                console.log(`Command closed with code: ${code}`);
                execNext(i + 1);
            }).on('data', (data) => {
                // console.log('STDOUT: ' + data);
            }).stderr.on('data', (data) => {
                // console.log('STDERR: ' + data);
            });
        });
    };
    
    execNext(0);
}).connect(sshConfig);
