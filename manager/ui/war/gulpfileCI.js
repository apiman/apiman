// Gulp File specifically for Cypress CI

const { gulp, connect } = require('./gulpfile.js');
const { createProxyMiddleware } = require('http-proxy-middleware');

// Connect Task
gulp.task('connect', function() {
// NextHandleFunction = (req: IncomingMessage, res: http.ServerResponse, next: NextFunction)
  connect.server({
    root: '.',
    livereload: false,
    host: '0.0.0.0',
    port: 2772,
    fallback: 'index.html',
    middleware: (_, __) => {
      function onProxyRes(proxyRes, req, res) {
        const location = proxyRes.headers['location'];
        if (location && location.includes(`${HOSTNAME}:8081`)) {
          proxyRes.headers['location'] =
              location.replace(`${HOSTNAME}:8081`, `${HOSTNAME}:2772`);
        }
        if (location && location.includes(`${HOSTNAME}:8085`)) {
          proxyRes.headers['location'] =
              location.replace(`${HOSTNAME}:8085`, `${HOSTNAME}:2772`);
        }
      }

      const HOSTNAME = process.env.HOSTNAME || 'localhost'

      const ssoProxy = createProxyMiddleware('/auth', {
        target: `http://${HOSTNAME}:8081`,
        changeOrigin: true, // for vhosted sites
        autoRewrite: true,
        followRedirect: false,
        xfwd: true,
        onProxyRes: onProxyRes,
        hostRewrite: {
          '*' : 'http://localhost:2772/'
        }
      });

      const apimanProxy = createProxyMiddleware('/apimanui', {
        target: `http://${HOSTNAME}:8085`,
        changeOrigin: true,
        autoRewrite: true,
        followRedirect: false,
        xfwd: true,
        onProxyRes: onProxyRes
      });

      const apimanProxy2 = createProxyMiddleware('/apiman', {
        target: `http://${HOSTNAME}:8085`,
        changeOrigin: true,
        autoRewrite: true,
        followRedirect: false,
        logLevel: "debug",
        xfwd: true,
        onProxyRes: onProxyRes
      });

      const apimanProxy3 = createProxyMiddleware('/apiman-gateway-api', {
        target: `http://${HOSTNAME}:8085`,
        changeOrigin: true,
        autoRewrite: true,
        followRedirect: false,
        logLevel: "debug",
        xfwd: true,
        onProxyRes: onProxyRes
      });

      const permissiveCors = [(req, res, next) => {
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Methods', 'GET,PUT,POST,PATCH,OPTIONS,HEAD');
        next();
      }]
      return [ssoProxy, apimanProxy, apimanProxy2, apimanProxy3, permissiveCors];
    }
  });
});

