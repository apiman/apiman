module.exports = {
    auth: {
      username: 'admin',
      password: 'admin123!'
    },
    strictSSL: false,
    followRedirect: false,
    validateStatus: function (status) {
        return (status / 100) === 2 || status === 401;
    }
  };