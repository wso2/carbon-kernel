var tokenName = "${CSRF_TOKEN_NAME}";
var tokenValue = "${CSRF_TOKEN_VALUE}";

/**
 * Injects CSRF token as a hidden input in every form found in the document
 * @param tokenName CSRF Token param name
 * @param tokenValue CSRF Token value
 */
function injectTokenForm(tokenName, tokenValue) {

    var forms = document.getElementsByTagName('form');
    for (var i = 0; i < forms.length; i++) {
        var method = forms[i].getAttribute("method");

        if ((typeof method != 'undefined') && method != null && method.toLowerCase() == "get") {
            return;
        }

        var hidden = document.createElement("input");

        hidden.setAttribute("type", "hidden");
        hidden.setAttribute("name", tokenName);
        hidden.setAttribute("value", tokenValue);

        forms[i].appendChild(hidden);
    }
}

jQuery(document).ready(function () {

    injectTokenForm(tokenName, tokenValue);

    jQuery.ajaxPrefilter(function (options, originalOptions, jqXHR) {
        // Append CSRF token for every ajax POST
        if (options.type.toLowerCase() === "post") {
            options.data += options.data ? "&" : "";
            options.data += tokenName + "=" + tokenValue;
        }
    });

    // In case dynamic forms are added this will add the CSRF token as a hidden param for dynamic forms at submit event
    jQuery(document).delegate("form", "submit", function () {
        if (jQuery(this).attr('method').toLowerCase() === "post") {
            if (jQuery(this).find('input[name=' + tokenName + ']').length <= 0) {
                jQuery('<input />').attr('type', 'hidden')
                    .attr('name', tokenName)
                    .attr('value', tokenValue)
                    .appendTo("form");
            }
        }
    });
});



