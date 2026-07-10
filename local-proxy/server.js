const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const cors = require('cors');

const app = express();

// Enable CORS for all incoming requests
app.use(cors());

// Define the targets
const targets = {
    '/api/cinemana': 'https://cinemana.shabakaty.com',
    '/api/rating': 'https://rating.shabakaty.com',
    '/api/thumbnail': 'https://thumbnail.shabakaty.com',
    '/api/recommend': 'https://recommend.shabakaty.com',
    '/api/cdn': 'https://cdn.shabakaty.com'
};

// Setup proxy for each target
Object.entries(targets).forEach(([pathPrefix, targetUrl]) => {
    app.use(pathPrefix, createProxyMiddleware({
        target: targetUrl,
        changeOrigin: true,
        pathRewrite: {
            [`^${pathPrefix}`]: '', // Remove the base path before forwarding
        },
        onProxyReq: (proxyReq, req, res) => {
            // Add custom headers if required by Shabakaty
            proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36');
            if (targetUrl.includes('shabakaty')) {
                proxyReq.setHeader('Origin', targetUrl);
                proxyReq.setHeader('Referer', `${targetUrl}/`);
            }
        },
        onError: (err, req, res) => {
            console.error(`Proxy Error on ${pathPrefix}:`, err.message);
            res.status(500).json({ error: 'Proxy failed', details: err.message });
        }
    }));
});

// Health check endpoint
app.get('/', (req, res) => {
    res.send({ status: 'Local Proxy is running', timestamp: new Date() });
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`=================================================`);
    console.log(`🚀 Local Proxy is running on http://localhost:${PORT}`);
    console.log(`=================================================`);
    console.log(`Available Endpoints:`);
    Object.keys(targets).forEach(t => console.log(`- http://localhost:${PORT}${t}`));
    console.log(`=================================================`);
});
