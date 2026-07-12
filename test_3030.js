const { execSync } = require('child_process');
const js = `fetch('http://localhost:3030/api/cinemana/api/android/transcoddedFiles/id/3112286')
  .then(r=>r.json())
  .then(t=>{
    const url = t[0].videoUrl;
    fetch('http://localhost:3030/api/cdn' + url.substring(25), {method:'HEAD', redirect: 'manual'})
      .then(r=>{
        const loc = r.headers.get('location');
        fetch('http://localhost:3030/api/dynamic?target=' + encodeURIComponent(loc), {method: 'GET', headers: {range: 'bytes=0-100'}})
          .then(r2 => console.log('DYNAMIC 3030:', r2.status, 'Range:', r2.headers.get('content-range')));
      });
  })`;

const b64 = Buffer.from(js).toString('base64');
execSync(`node run_ssh.js "echo ${b64} | base64 -d | node"`, {stdio:'inherit'});
