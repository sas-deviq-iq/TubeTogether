
const { createProxyMiddleware } = require('http-proxy-middleware');
const express = require('express');
const app = express();

app.use('/api/dynamic', createProxyMiddleware({
    router: function(req) {
        return new URL(req.query.target).origin;
    },
    changeOrigin: true,
    pathRewrite: function(path, req) {
        const url = new URL(req.query.target);
        return url.pathname + url.search;
    },
    onProxyReq: (proxyReq, req, res) => {
        const url = new URL(req.query.target);
        proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36');
        proxyReq.setHeader('Origin', url.origin);
        proxyReq.setHeader('Referer', url.origin + '/');
    },
    onProxyRes: (proxyRes, req, res) => {
        if (proxyRes.statusCode >= 300 && proxyRes.statusCode < 400 && proxyRes.headers.location) {
            proxyRes.headers['location'] = 'http://REWRITTEN-SUCCESSFULLY';
        }
    }
}));

app.listen(3035);
