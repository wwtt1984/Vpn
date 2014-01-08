var Vpn = function () {};
/**
 * 设置提示值
 * @param user
 * @param pwd
 * @returns {*}
 */
Vpn.prototype.VpnLogin = function (callback) {
    return cordova.exec(callback, null,"VpnPlugin","Vpn",[]);
};

Vpn.prototype.VpnLoginFirst = function (callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnFirst",[]);
};

Vpn.prototype.VpnOnWifi = function (callback) {
    return cordova.exec(callback, null,"VpnPlugin","VpnOnWifi",[]);
};


module.exports = (new Vpn());
