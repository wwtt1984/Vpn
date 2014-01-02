var Vpn = function () {};
/**
 * 设置提示值
 * @param user
 * @param pwd
 * @returns {*}
 */
Vpn.prototype.VpnLogin = function (user,pwd) {
    return cordova.exec(null, null,"VpnPlugin","Vpn",[user,pwd]);
};
module.exports = (new Vpn());
