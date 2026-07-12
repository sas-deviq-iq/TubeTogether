const { execSync } = require('child_process');
const js = `fetch('http://158.220.120.204:8080/api/cinemana/api/android/transcoddedFiles/id/3112286')
  .then(r=>r.json())
  .then(t=>{
    const url = t[0].videoUrl;
    fetch('http://localhost:3035/api/dynamic?target=' + encodeURIComponent(url), {method:'HEAD', redirect: 'manual'})
      .then(r=>console.log('DYNAMIC PROXY 3035:', r.status, r.headers.get('location')));
  })`;

const b64 = Buffer.from(js).toString('base64');
execSync(`node run_ssh.js "echo ${b64} | base64 -d | node"`, {stdio:'inherit'});
