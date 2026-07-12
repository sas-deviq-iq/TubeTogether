const { execSync } = require('child_process');
const js = `fetch('http://localhost:3030/api/cinemana/api/android/transcoddedFiles/id/3112286')
  .then(r=>r.json())
  .then(t=>{
    const url = t[0].videoUrl;
    fetch('http://localhost:3030/api/cdn' + url.substring(25), {method:'HEAD', redirect: 'manual'})
      .then(r=>{
        const loc = r.headers.get('location');
        fetch(loc, {method: 'HEAD'}).then(r2 => console.log('NATIVE HEAD ON VPS:', r2.status));
      });
  })`;

const b64 = Buffer.from(js).toString('base64');
execSync(`node run_ssh.js "echo ${b64} | base64 -d | node"`, {stdio:'inherit'});
