var Vpn = function () {};
/**
 * 设置提示值
 * @param user
 * @param pwd
 * @returns {*}
 */
Vpn.prototype.VpnLogin = function (user,pwd,callback) {
    return cordova.exec(callback, null,"VpnPlugin","Vpn",[user,pwd]);
};

Vpn.prototype.VpnOnWifi = function (ip,callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnOnWifi",[ip]);
};

Vpn.prototype.VpnCheckOnLine = function (user,pwd,callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnCheckOnLine",[user,pwd]);
};

Vpn.prototype.VpnOFF = function (callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnOFF",[]);
};

Vpn.prototype.VpnReset = function (callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnReset",[]);
};

Vpn.prototype.VpnGPSON = function (callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnGPSON",[]);
};

Vpn.prototype.VpnGPSSet = function () {
    return cordova.exec(null, null,"VpnPlugin","VpnGPSSet",[]);
};

Vpn.prototype.VpnInputON = function () {
    return cordova.exec(null, null,"VpnPlugin","VpnInputON",[]);
};


module.exports = (new Vpn());
