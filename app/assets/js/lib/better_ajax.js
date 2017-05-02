/**
 * inspired by https://github.com/kylebarrow/chibi/blob/master/chibi.js
 */
var request = (function(){
    var self = {};

    self.ajax = function(url, method, data, result_type, on_success, on_error) {
        return $.ajax({
                type: method,
                url: url,
                data: self.serializeData(data),
                contentType: self.contentType(data),
                dataType: result_type,
                success: on_success,
                error: function(jqXHR, textStatus){
                    on_error(self.handle_error(jqXHR, textStatus));
                }
        });
    }

    self.get = function(url, data, on_success, on_error) {
        return self.ajax(url, 'GET', data, 'json', on_success, on_error);
    }

    self.post = function(url, data, on_success, on_error) {
        return self.ajax(url, 'POST', data, 'json', on_success, on_error);
    };

    self.handle_error = function(jqXHR, textStatus) {
        var err = {
            user_msg: "",
            tech_msg: "",
            code: ""
        };
        var err_msg_tmpl = "@%=user_msg %@ ( @%=code %@ @%=tech_msg %@ )";

        if(jqXHR.responseJSON && typeof jqXHR.responseJSON == "object"){
            return jqXHR.responseJSON;
        } else if(jqXHR.responseJSON && isArray(jqXHR.responseJSON.length)){
            var err_json = jqXHR.responseJSON[0];
            err.user_msg = err_json.message;
            err.tech_msg = err_json.technicalCode;
        } else {
            err.user_msg = jqXHR.responseText;
            err.code = jqXHR.status;
            err.tech_msg = jqXHR.statusText;
        }

        var err_msg = tmpl(err_msg_tmpl, err);

        console.log(err_msg);
        return err_msg;
    }

    // Serialize form & JSON values
    self.serializeData = function(nodes) {
		if (nodes.constructor === Object) { // Serialize JSON data
		    return JSON.stringify(nodes);
		} else { // Serialize jquery selector
		    return $(nodes).serialize();
		}
	}

	self.contentType = function(obj_to_send) {
	    if (obj_to_send.constructor === Object) { // Serialize JSON data
		    return 'application/json; charset=utf-8';
		} else { // Serialize jquery selector
		    return 'application/x-www-form-urlencoded; charset=utf-8';
		}
	}

    return self;
})();
