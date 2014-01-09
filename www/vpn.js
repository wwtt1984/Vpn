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

Vpn.prototype.VpnOnWifi = function (callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnOnWifi",[]);
};

module.exports = (new Vpn());
