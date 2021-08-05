const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const https = require('https');
const assert = require('assert');
const path = require("path");
const fs = require("fs");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    let page = await cas.newPage(browser);
    const service = "https://example.com";
    await page.goto("https://localhost:8443/cas/login?service=" + service);
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");

    let ticket = await cas.assertTicketParameter(page);

    let options = {
        protocol: 'https:',
        hostname: 'localhost',
        port: 8443,
        path: '/cas/validate?service=' + service + "&ticket=" + ticket,
        method: 'GET',
        rejectUnauthorized: false,
    };

    const httpGet = options => {
        return new Promise((resolve, reject) => {
            https.get(options, res => {
                res.setEncoding('utf8');
                const body = [];
                res.on('data', chunk => body.push(chunk));
                res.on('end', () => resolve(body.join('')));
            }).on('error', reject);
        });
    };
    await httpGet(options);
    await page.close();
    
    page = await cas.newPage(browser);
    await page.setRequestInterception(true);

    page.on('request', request => {
        let data = {
            'method': 'POST',
            'postData': 'SAMLRequest=PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOkxvZ291dFJlcXVlc3QgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIERlc3RpbmF0aW9uPSJodHRwczovL2xvY2FsaG9zdDo4NDQzL2Nhcy9pZHAvcHJvZmlsZS9TQU1MMi9QT1NUL1NMTyIgSUQ9IjEyMzQiIElzc3VlSW5zdGFudD0iMjAyMS0wNi0wMVQxMDowNDoyMi43NzJaIiBWZXJzaW9uPSIyLjAiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHBzOi8vc2FtbHRlc3QuaWQvc2FtbC9zcDwvc2FtbDI6SXNzdWVyPjxzYW1sMjpOYW1lSUQgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6dHJhbnNpZW50Ij5mYWtlbmFtZWlkPC9zYW1sMjpOYW1lSUQ%2BPHNhbWwycDpTZXNzaW9uSW5kZXg%2BZmFrZXNlc3Npb25pbmRleDwvc2FtbDJwOlNlc3Npb25JbmRleD48L3NhbWwycDpMb2dvdXRSZXF1ZXN0Pg%3D%3D',
            'headers': {
                ...request.headers(),
                'Content-Type': 'application/x-www-form-urlencoded'
            },
        };

        request.continue(data);
    });

    await page.goto('https://localhost:8443/cas/idp/profile/SAML2/POST/SLO');
    const content = await page.content();
    assert(content.includes('value="Go to https://samltest.id/Shibboleth.sso/SLO/Redirect?SAMLResponse='));

    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdir(metadataDir, {recursive: true}, () => {
    });

    await browser.close();
})();
