const { Client } = require('ssh2');
const fs = require('fs');

let content = fs.readFileSync('temp_server.js', 'utf8');
content = content.replace("const location = proxyRes.headers.location;", "const location = proxyRes.headers.location; console.log('Intercepted redirect to:', location);");
fs.writeFileSync('temp_server.js', content);

const conn = new Client();
conn.on('ready', () => {
    console.log('Client :: ready');
    conn.sftp((err, sftp) => {
        if (err) throw err;
        const readStream = fs.createReadStream('temp_server.js');
        const writeStream = sftp.createWriteStream('/home/sas-dev/cinemana-proxy/server.js');
        
        writeStream.on('close', () => {
            console.log('Upload complete');
            conn.exec('pm2 restart proxy', (err, stream) => {
                if (err) throw err;
                stream.on('close', (code, signal) => {
                    console.log('Stream :: close :: code: ' + code + ', signal: ' + signal);
                    conn.end();
                }).on('data', (data) => {
                    console.log('STDOUT: ' + data);
                }).stderr.on('data', (data) => {
                    console.log('STDERR: ' + data);
                });
            });
        });
        
        readStream.pipe(writeStream);
    });
}).connect({
    host: '192.168.0.34',
    port: 22,
    username: 'sas-dev',
    password: '07702098319mustafa'
});
