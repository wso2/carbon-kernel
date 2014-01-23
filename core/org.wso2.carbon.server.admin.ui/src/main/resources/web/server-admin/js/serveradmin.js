function shutdownServerGracefully() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["graceful.shutdown.verify"],shutdownServerGracefullyCallback,null);

}

function shutdownServerGracefullyCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=shutdownGracefully";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
/*        if (jQuery.trim(responseText) != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }*/
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["graceful.shutdown.error"]);
        } else {
            CARBON.showInfoDialog(jsi18n["graceful.shutdown.in.progress.message"]);
        }
    });
}

function shutdownServer() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["shutdown.verification"],shutdownServerCallback,null);
}

function shutdownServerCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=shutdown";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
/*        if (jQuery.trim(responseText) != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }*/
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["shutdown.error"]);
        } else {
            CARBON.showInfoDialog(jsi18n["shutdown.in.progress.message"]);
        }
    });
}

function restartServerGracefully() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["graceful.restart.verification"],restartServerGracefullyCallback,null);
}

function restartServerGracefullyCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=restartGracefully";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
/*        if (jQuery.trim(responseText) != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }*/
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["graceful.restart.error"]);
        } else {
            CARBON.showInfoDialog(jsi18n["graceful.restart.in.progress.message"]);
        }
    });
}

function restartServer() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["restart.verification"],restartServerCallback,null);
}

function restartServerCallback() {
    var url = "proxy_ajaxprocessor.jsp?action=restart";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
/*        if (jQuery.trim(responseText) != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }*/
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["restart.error"]);
        } else {
            CARBON.showInfoDialog(jsi18n["restart.in.progress.message"]);
        }
    });
}