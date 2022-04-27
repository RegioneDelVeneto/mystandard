const PROXY_CONFIG = {
  "/": {
      "target": "http://localhost:8080",
      "secure": true,
      // "logLevel": "debug",
      "onProxyRes": function (proxyRes, req, res) {
        proxyRes.headers['Access-Control-Allow-Headers'] = '*';
      },
    },
};

module.exports = PROXY_CONFIG;