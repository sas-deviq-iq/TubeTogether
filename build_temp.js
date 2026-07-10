const fs = require('fs');

let content = `const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const cors = require('cors');
const https = require('https');

const app = express();
app.use(cors());

const httpsAgent = new https.Agent({
    keepAlive: true,
    maxSockets: 100
});

app.use('/api/dynamic', (req, res) => {
    const targetUrl = req.query.target;
    if (!targetUrl) {
        return res.status(400).send('Missing target');
    }
    
    let parsed;
    try {
        parsed = new URL(targetUrl);
    } catch(e) {
        return res.status(400).send('Invalid target URL');
    }

    const options = {
        hostname: parsed.hostname,
        port: 443,
        path: parsed.pathname + parsed.search,
        method: req.method,
        agent: httpsAgent,
        headers: {
            'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
            'origin': parsed.origin,
            'referer': \`\${parsed.origin}/\`
        }
    };
    
    // Copy necessary headers from the original request
    if (req.headers.range) {
        options.headers.range = req.headers.range;
    }
    if (req.headers.accept) {
        options.headers.accept = req.headers.accept;
    }

    const proxyReq = https.request(options, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res, { end: true });
    });

    proxyReq.on('error', (err) => {
        console.error('Manual Proxy Error:', err);
        if (!res.headersSent) {
            res.status(500).send('Proxy Error');
        }
    });

    // IMPORTANT: If ExoPlayer disconnects (e.g. seeking), abort the download!
    req.on('close', () => {
        proxyReq.destroy();
    });

    if (req.method === 'GET' || req.method === 'HEAD') {
        proxyReq.end();
    } else {
        req.pipe(proxyReq, { end: true });
    }
});

const targets = {
    '/api/cinemana': 'https://cinemana.shabakaty.com',
    '/api/rating': 'https://rating.shabakaty.com',
    '/api/thumbnail': 'https://thumbnail.shabakaty.com',
    '/api/recommend': 'https://recommend.shabakaty.com',
    '/api/cdn': 'https://cdn.shabakaty.com'
};

Object.entries(targets).forEach(([pathPrefix, targetUrl]) => {
    app.use(pathPrefix, createProxyMiddleware({
        target: targetUrl,
        changeOrigin: true,
        pathRewrite: (path, req) => path.replace(pathPrefix, ''),
        onProxyReq: (proxyReq, req, res) => {
            proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36');
            if (targetUrl.includes('shabakaty')) {
                proxyReq.setHeader('Origin', targetUrl);
                proxyReq.setHeader('Referer', \`\${targetUrl}/\`);
            }
        },
        onError: (err, req, res) => {
            console.error('Proxy Error:', err.message);
            if (!res.headersSent) res.status(500).json({ error: 'Proxy failed', details: err.message });
        }
    }));
});

app.get('/', (req, res) => {
    res.send({ status: 'Local Proxy is running', timestamp: new Date() });
});

const PORT = 3030;
app.listen(PORT, () => {
    console.log(\`Local Proxy is running on http://localhost:\${PORT}\`);
});
`;

fs.writeFileSync('temp_server.js', content);
