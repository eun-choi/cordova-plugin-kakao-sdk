var exec = require('cordova/exec');

var KakaoCordovaSDK = {
  login: function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'KakaoCordovaSDK', 'login', []);
  },

  logout: function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'KakaoCordovaSDK', 'logout', []);
  },
};

module.exports = KakaoCordovaSDK;
