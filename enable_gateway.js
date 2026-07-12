const { Client } = require('ssh2');
const conn = new Client();
conn.on('ready', () => {
  conn.exec('echo "GatewayPorts yes" >> /etc/ssh/sshd_config && systemctl restart ssh || systemctl restart sshd', (err, stream) => {
    if (err) throw err;
    stream.on('close', (code, signal) => {
      conn.end();
    }).on('data', (data) => {
      console.log('STDOUT: ' + data);
    }).stderr.on('data', (data) => {
      console.log('STDERR: ' + data);
    });
  });
}).connect({
  host: '158.220.120.204',
  port: 22,
  username: 'root',
  password: '07702098319Mustafa@'
});
