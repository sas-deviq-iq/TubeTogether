const { Client } = require('ssh2');
const conn = new Client();
const command = process.argv[2] || 'whoami';
conn.on('ready', () => {
  conn.exec(command, (err, stream) => {
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
  host: '192.168.0.34',
  port: 22,
  username: 'sas-dev',
  password: '07702098319mustafa'
});
