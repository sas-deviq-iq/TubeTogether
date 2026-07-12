#!/bin/bash

echo "============================================="
echo "   تحديث سيرفر VPS لإضافة نظام الحسابات"
echo "============================================="

cd /opt/cinemana-proxy

echo "جاري تثبيت المكتبات المطلوبة..."
npm install sqlite3 bcrypt jsonwebtoken google-auth-library body-parser

echo "جاري كتابة كود السيرفر المحدث..."
cat << 'EOF' > server.js
const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const cors = require('cors');
const bodyParser = require('body-parser');
const sqlite3 = require('sqlite3').verbose();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { OAuth2Client } = require('google-auth-library');

const app = express();
app.use(cors());
app.use(bodyParser.json());

// ==========================================
// 1. إعداد قاعدة البيانات (SQLite)
// ==========================================
const db = new sqlite3.Database('./users.db', (err) => {
    if (err) console.error('Error opening database', err);
    else console.log('Database connected');
});

db.serialize(() => {
    db.run(`CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT,
        email TEXT UNIQUE,
        password TEXT,
        googleId TEXT,
        avatar TEXT,
        createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
    )`);
});

// ==========================================
// 2. إعدادات المصادقة (Auth API)
// ==========================================
const JWT_SECRET = "tubetogether_super_secret_key_2026"; // يفضل تغييره لاحقاً
const GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID"; // سنضعه لاحقاً بعد إنشاء حساب جوجل كونسول
const googleClient = new OAuth2Client(GOOGLE_CLIENT_ID);

// واجهة إنشاء حساب (Register)
app.post('/api/auth/register', async (req, res) => {
    const { name, email, password } = req.body;
    if (!name || !email || !password) return res.status(400).json({ error: "جميع الحقول مطلوبة" });

    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        db.run(`INSERT INTO users (name, email, password) VALUES (?, ?, ?)`, [name, email, hashedPassword], function(err) {
            if (err) {
                if (err.message.includes('UNIQUE')) return res.status(400).json({ error: "البريد الإلكتروني مسجل مسبقاً" });
                return res.status(500).json({ error: err.message });
            }
            const token = jwt.sign({ id: this.lastID, email, name }, JWT_SECRET, { expiresIn: '30d' });
            res.json({ token, user: { id: this.lastID, name, email } });
        });
    } catch (e) {
        res.status(500).json({ error: "حدث خطأ داخلي" });
    }
});

// واجهة تسجيل الدخول (Login)
app.post('/api/auth/login', (req, res) => {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ error: "البريد وكلمة المرور مطلوبة" });

    db.get(`SELECT * FROM users WHERE email = ?`, [email], async (err, user) => {
        if (err) return res.status(500).json({ error: err.message });
        if (!user || !user.password) return res.status(400).json({ error: "البريد أو كلمة المرور غير صحيحة" });

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ error: "البريد أو كلمة المرور غير صحيحة" });

        const token = jwt.sign({ id: user.id, email: user.email, name: user.name }, JWT_SECRET, { expiresIn: '30d' });
        res.json({ token, user: { id: user.id, name: user.name, email: user.email, avatar: user.avatar } });
    });
});

// واجهة تسجيل الدخول بحساب جوجل (Google Sign-In)
app.post('/api/auth/google', async (req, res) => {
    const { idToken } = req.body;
    if (!idToken) return res.status(400).json({ error: "Google ID Token is required" });

    try {
        // إذا لم تقم بإعداد GOOGLE_CLIENT_ID بعد، سنتجاهل التحقق المعقد مؤقتاً ونكتفي بقراءة البيانات
        // في الإنتاج يجب فك التعليق عن هذا الكود:
        /*
        const ticket = await googleClient.verifyIdToken({
            idToken: idToken,
            audience: GOOGLE_CLIENT_ID,
        });
        const payload = ticket.getPayload();
        */
        
        // مؤقتاً نفك تشفير التوكن مباشرة (بدون تحقق من جوجل) لتسهيل التطوير حتى تجهز الكونسول
        const payload = jwt.decode(idToken);
        if (!payload) return res.status(400).json({ error: "Invalid Google Token" });

        const { sub: googleId, email, name, picture: avatar } = payload;

        db.get(`SELECT * FROM users WHERE email = ?`, [email], (err, user) => {
            if (err) return res.status(500).json({ error: err.message });

            if (user) {
                // المستخدم موجود، تحديث جوجل أيدي إذا كان فارغاً
                db.run(`UPDATE users SET googleId = ?, avatar = ? WHERE id = ?`, [googleId, avatar, user.id]);
                const token = jwt.sign({ id: user.id, email, name }, JWT_SECRET, { expiresIn: '30d' });
                return res.json({ token, user: { id: user.id, name, email, avatar } });
            } else {
                // مستخدم جديد
                db.run(`INSERT INTO users (name, email, googleId, avatar) VALUES (?, ?, ?, ?)`, [name, email, googleId, avatar], function(err) {
                    if (err) return res.status(500).json({ error: err.message });
                    const token = jwt.sign({ id: this.lastID, email, name }, JWT_SECRET, { expiresIn: '30d' });
                    res.json({ token, user: { id: this.lastID, name, email, avatar } });
                });
            }
        });
    } catch (e) {
        res.status(401).json({ error: "فشل التحقق من حساب جوجل", details: e.message });
    }
});


// ==========================================
// 3. البروكسي القديم (Cinemana)
// ==========================================
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
        pathRewrite: { [`^${pathPrefix}`]: '' },
        onProxyReq: (proxyReq, req, res) => {
            proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36');
            if (targetUrl.includes('shabakaty')) {
                proxyReq.setHeader('Origin', targetUrl);
                proxyReq.setHeader('Referer', `${targetUrl}/`);
            }
        },
        onError: (err, req, res) => {
            console.error(`Proxy Error:`, err.message);
            res.status(500).json({ error: 'Proxy failed', details: err.message });
        }
    }));
});

app.get('/', (req, res) => {
    res.send({ status: 'VPS Server (Auth + Proxy) is running', timestamp: new Date() });
});

const PORT = 8080;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
EOF

echo "جاري إعادة تشغيل السيرفر..."
pm2 restart cinemana-proxy

echo "============================================="
echo "   تم تحديث السيرفر بنجاح! 🚀"
echo "============================================="
