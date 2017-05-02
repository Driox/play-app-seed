/**
 * Allow to access url params easilly
 */
var url_utils = (function(){
    var self = {};

    self.goToWithParam = function(url, param_query) {
        window.location.replace(addParamToUrl(url, param_query));
    };

    self.goTo = function(url) {
        var win = window.open(url, "_blank");
        win.focus();
    };

    self.get_params = function(sParam) {
        var sPageURL = decodeURIComponent(window.location.search.substring(1)),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === sParam) {
                return sParameterName[1] === undefined ? true : sParameterName[1];
            }
        }
    };

    return self;
})();
