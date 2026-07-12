#!/bin/bash

echo "============================================="
echo "   تحديث سيرفر VPS لإضافة نظام الحسابات والتشفير"
echo "============================================="

cd /opt/cinemana-proxy

echo "جاري تثبيت المكتبات المطلوبة..."
npm install sqlite3 bcrypt jsonwebtoken google-auth-library body-parser

echo "جاري كتابة كود السيرفر المحدث..."
cat << 'EOF' > server.js
const express = require('express');
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
// 1. إعداد قاعدة البيانات و مسارات المصادقة
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
        username TEXT UNIQUE,
        dob TEXT,
        country TEXT,
        profile_complete BOOLEAN DEFAULT 0
    )`);
    
    // Add columns safely if they don't exist
    // NOTE: SQLite forbids UNIQUE on ALTER TABLE ADD COLUMN (it fails silently here
    // since errors are ignored) - add the column plain, then enforce uniqueness via index.
    try {
        db.run("ALTER TABLE users ADD COLUMN username TEXT", () => {});
        db.run("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username)", () => {});
        db.run("ALTER TABLE users ADD COLUMN dob TEXT", () => {});
        db.run("ALTER TABLE users ADD COLUMN country TEXT", () => {});
        db.run("ALTER TABLE users ADD COLUMN profile_complete BOOLEAN DEFAULT 0", () => {});
    } catch(e) {}
});

// ==========================================
// 2. إعدادات المصادقة (Auth API)
// ==========================================
const JWT_SECRET = "tubetogether_super_secret_key_2026";
const GOOGLE_CLIENT_ID = "857976548779-m82ksachjgm7leko8q51uts7k2c24dhn.apps.googleusercontent.com";
const googleClient = new OAuth2Client(GOOGLE_CLIENT_ID);

app.post('/api/auth/register', async (req, res) => {
    const { name, email, password } = req.body;
    if (!name || !email || !password) return res.status(400).json({ error: "جميع الحقول مطلوبة" });

    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        db.run(`INSERT INTO users (name, email, password, profile_complete) VALUES (?, ?, ?, 0)`, [name, email, hashedPassword], function(err) {
            if (err) {
                if (err.message.includes('UNIQUE')) return res.status(400).json({ error: "البريد الإلكتروني مسجل مسبقاً" });
                return res.status(500).json({ error: err.message });
            }
            const token = jwt.sign({ id: this.lastID, email, name }, JWT_SECRET, { expiresIn: '30d' });
            res.json({ token, user: { id: this.lastID, name, email, profile_complete: 0 } });
        });
    } catch (e) {
        res.status(500).json({ error: "حدث خطأ داخلي" });
    }
});

app.post('/api/auth/login', (req, res) => {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ error: "البريد وكلمة المرور مطلوبة" });

    db.get(`SELECT * FROM users WHERE email = ?`, [email], async (err, user) => {
        if (err) return res.status(500).json({ error: err.message });
        if (!user || !user.password) return res.status(400).json({ error: "البريد أو كلمة المرور غير صحيحة" });

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ error: "الإيميل أو كلمة المرور غير صحيحة" });

        const token = jwt.sign({ id: user.id, email: user.email, name: user.name }, JWT_SECRET, { expiresIn: '30d' });
        res.json({ token, user: { id: user.id, name: user.name, email: user.email, avatar: user.avatar, profile_complete: user.profile_complete || 0 } });
    });
});

app.post('/api/auth/google', async (req, res) => {
    const { idToken } = req.body;
    if (!idToken) return res.status(400).json({ error: "Google ID Token is required" });

    try {
        const payload = jwt.decode(idToken);
        if (!payload) return res.status(400).json({ error: "Invalid Google Token" });

        const { sub: googleId, email, name, picture: avatar } = payload;

        db.get(`SELECT * FROM users WHERE email = ?`, [email], (err, user) => {
            if (err) return res.status(500).json({ error: err.message });

            if (user) {
                db.run(`UPDATE users SET googleId = ?, avatar = ? WHERE id = ?`, [googleId, avatar, user.id]);
                const token = jwt.sign({ id: user.id, email, name }, JWT_SECRET, { expiresIn: '30d' });
                return res.json({ token, user: { id: user.id, name: user.name || name, email, avatar, profile_complete: user.profile_complete || 0 } });
            } else {
                db.run(`INSERT INTO users (name, email, googleId, avatar, profile_complete) VALUES (?, ?, ?, ?, 0)`, [name, email, googleId, avatar], function(err) {
                    if (err) return res.status(500).json({ error: err.message });
                    const token = jwt.sign({ id: this.lastID, email, name }, JWT_SECRET, { expiresIn: '30d' });
                    res.json({ token, user: { id: this.lastID, name, email, avatar, profile_complete: 0 } });
                });
            }
        });
    } catch (e) {
        res.status(401).json({ error: "فشل التحقق من توكن جوجل", details: e.message });
    }
});

app.post('/api/auth/complete-profile', (req, res) => {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ error: "Unauthorized" });
    
    const token = authHeader.split(' ')[1];
    jwt.verify(token, JWT_SECRET, (err, decoded) => {
        if (err) return res.status(401).json({ error: 'Invalid or expired token.' });
        
        const { name, username, dob, country } = req.body;
        if (!name || !username || !dob || !country) {
            return res.status(400).json({ error: "يرجى تعبئة جميع الحقول" });
        }
        
        db.run(`UPDATE users SET name = ?, username = ?, dob = ?, country = ?, profile_complete = 1 WHERE id = ?`, 
        [name, username, dob, country, decoded.id], function(err) {
            if (err) {
                if (err.message.includes('UNIQUE')) return res.status(400).json({ error: "اسم المستخدم هذا محجوز مسبقاً، اختر اسماً آخر." });
                return res.status(500).json({ error: err.message });
            }
            res.json({ success: true, profile_complete: 1 });
        });
    });
});

app.get('/api/auth/me', (req, res) => {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ error: "Unauthorized" });

    const token = authHeader.split(' ')[1];
    jwt.verify(token, JWT_SECRET, (err, decoded) => {
        if (err) return res.status(401).json({ error: 'Invalid or expired token.' });

        db.get(`SELECT id, name, email, username, avatar, dob, country, profile_complete FROM users WHERE id = ?`, [decoded.id], (err, user) => {
            if (err) return res.status(500).json({ error: err.message });
            if (!user) return res.status(404).json({ error: 'User not found' });
            res.json(user);
        });
    });
});

// ==========================================
// 3. جدار الحماية (Auth Enforcement)
// ==========================================
app.use((req, res, next) => {
    // استثناء واجهات التسجيل والفحص من التحقق
    if (req.path.startsWith('/api/auth/') || req.path === '/') return next();

    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        console.log("Blocked unauthorized request from old app version:", req.path);
        return res.status(401).json({ error: 'Unauthorized: You must login to use the app.' });
    }

    const token = authHeader.split(' ')[1];
    jwt.verify(token, JWT_SECRET, (err, decoded) => {
        if (err) return res.status(401).json({ error: 'Invalid or expired token.' });
        req.user = decoded;
        next();
    });
});

// ==========================================
// 4. مصدر بيانات المحتوى (TMDB)
// ==========================================
// المفتاح يُقرأ من ملف محلي غير مرفوع لـ git أبداً (secrets.json بجانب هذا الملف).
let TMDB_API_KEY = '';
try {
    TMDB_API_KEY = require('./secrets.json').tmdbApiKey || '';
} catch (e) {
    console.warn('secrets.json غير موجود - واجهة محتوى TMDB لن تعمل حتى يُضاف المفتاح.');
}

const TMDB_BASE = 'https://api.themoviedb.org/3';
const TMDB_IMG_BASE = 'https://image.tmdb.org/t/p';

async function tmdbGet(path, params = {}) {
    const url = new URL(TMDB_BASE + path);
    url.searchParams.set('api_key', TMDB_API_KEY);
    url.searchParams.set('language', 'ar');
    Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, v));
    const response = await fetch(url.toString());
    if (!response.ok) throw new Error(`TMDB ${path} failed: ${response.status}`);
    return response.json();
}

function tmdbImg(path, size) {
    return path ? `${TMDB_IMG_BASE}/${size}${path}` : null;
}

function mapMovie(m) {
    return {
        nb: `movie:${m.id}`, id: `movie:${m.id}`,
        en_title: m.title, ar_title: m.title, title: m.title,
        year: (m.release_date || '').slice(0, 4) || null,
        kind: "1",
        imgMediumThumbObjUrl: tmdbImg(m.poster_path, 'w342'),
        imgObjUrl: tmdbImg(m.poster_path, 'w780'),
        ar_content: m.overview, en_content: m.overview,
        stars: m.vote_average ? m.vote_average.toFixed(1) : null,
    };
}

function mapTv(t) {
    return {
        nb: `tv:${t.id}`, id: `tv:${t.id}`,
        en_title: t.name, ar_title: t.name, title: t.name,
        year: (t.first_air_date || '').slice(0, 4) || null,
        kind: "2",
        imgMediumThumbObjUrl: tmdbImg(t.poster_path, 'w342'),
        imgObjUrl: tmdbImg(t.poster_path, 'w780'),
        ar_content: t.overview, en_content: t.overview,
        stars: t.vote_average ? t.vote_average.toFixed(1) : null,
    };
}

function parseContentId(id) {
    const parts = String(id).split(':');
    return { type: parts[0], tmdbId: parts[1] };
}

app.get('/api/content/groups', async (req, res) => {
    try {
        const [trending, popularMovies, popularTv, topRated] = await Promise.all([
            tmdbGet('/trending/all/week'),
            tmdbGet('/movie/popular'),
            tmdbGet('/tv/popular'),
            tmdbGet('/movie/top_rated'),
        ]);
        const groups = [
            {
                title: "الأكثر رواجاً",
                content: (trending.results || [])
                    .filter(i => i.media_type === 'movie' || i.media_type === 'tv')
                    .map(i => i.media_type === 'tv' ? mapTv(i) : mapMovie(i)),
            },
            { title: "أفلام شائعة", content: (popularMovies.results || []).map(mapMovie) },
            { title: "مسلسلات شائعة", content: (popularTv.results || []).map(mapTv) },
            { title: "الأعلى تقييماً", content: (topRated.results || []).map(mapMovie) },
        ];
        res.json({ groups });
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

app.get('/api/content/search', async (req, res) => {
    try {
        const { query, type } = req.query;
        if (!query) return res.json([]);
        if (type === 'series') {
            const data = await tmdbGet('/search/tv', { query });
            return res.json((data.results || []).map(mapTv));
        }
        const data = await tmdbGet('/search/movie', { query });
        res.json((data.results || []).map(mapMovie));
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

app.get('/api/content/details/:id', async (req, res) => {
    try {
        const { type, tmdbId } = parseContentId(req.params.id);
        const isTv = type === 'tv';
        const [details, credits] = await Promise.all([
            tmdbGet(`/${isTv ? 'tv' : 'movie'}/${tmdbId}`),
            tmdbGet(`/${isTv ? 'tv' : 'movie'}/${tmdbId}/credits`),
        ]);
        const mapped = isTv ? mapTv(details) : mapMovie(details);
        mapped.categories = (details.genres || []).map(g => ({ en_title: g.name, ar_title: g.name }));
        mapped.actorsInfo = (credits.cast || []).slice(0, 15).map(c => ({
            nb: String(c.id), name: c.name, staff_img: tmdbImg(c.profile_path, 'w185'),
        }));
        res.json(mapped);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

app.get('/api/content/episodes/:tvId', async (req, res) => {
    try {
        const tvId = req.params.tvId;
        const tvDetails = await tmdbGet(`/tv/${tvId}`);
        const seasonNumbers = (tvDetails.seasons || [])
            .map(s => s.season_number)
            .filter(n => n > 0); // تجاهل "المواسم الخاصة" (season 0)

        const seasonResults = await Promise.all(
            seasonNumbers.map(n => tmdbGet(`/tv/${tvId}/season/${n}`).catch(() => null))
        );

        const episodes = [];
        seasonResults.forEach(season => {
            if (!season || !season.episodes) return;
            season.episodes.forEach(ep => {
                episodes.push({
                    nb: `tv:${tvId}:${ep.season_number}:${ep.episode_number}`,
                    id: `tv:${tvId}:${ep.season_number}:${ep.episode_number}`,
                    season: String(ep.season_number),
                    episodeNummer: String(ep.episode_number),
                    en_title: ep.name, ar_title: ep.name, title: ep.name,
                    imgMediumThumbObjUrl: tmdbImg(ep.still_path, 'w300') || tmdbImg(tvDetails.poster_path, 'w342'),
                });
            });
        });
        res.json(episodes);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

app.get('/', (req, res) => {
    res.send({ status: 'VPS Server (Auth + TMDB Content API) is running', timestamp: new Date() });
});

const PORT = 8080;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
EOF

echo "جاري إعادة تشغيل السيرفر..."
pm2 restart cinemana-proxy

echo "============================================="
echo "   تم تحديث السيرفر وتفعيل جدار الحماية بنجاح! 🚀"
echo "============================================="
