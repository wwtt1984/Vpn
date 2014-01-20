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

module.exports = (new Vpn());
