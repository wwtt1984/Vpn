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

module.exports = (new Vpn());
