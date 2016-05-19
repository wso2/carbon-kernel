function shutdownServerGracefully() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["graceful.shutdown.verify"],shutdownServerGracefullyCallback,null);

}

function shutdownServerGracefullyCallback() {
    jQuery.noConflict();
    jQuery.post("proxy_ajaxprocessor.jsp",
        {
            "action":"shutdownGracefully"
        },function (responseText, status, XMLHttpRequest) {
            /*        if (jQuery.trim(responseText) != '') {
                CARBON.showWarningDialog(responseText);
                return;
            }*/
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["graceful.shutdown.error"]);
            } else {
                CARBON.showInfoDialog(jsi18n["graceful.shutdown.in.progress.message"]);
            }
        }
    );
}

function shutdownServer() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["shutdown.verification"],shutdownServerCallback,null);
}

function shutdownServerCallback() {
    jQuery.noConflict();
    jQuery.post("proxy_ajaxprocessor.jsp",
        {
            "action":"shutdown"
        },function (responseText, status, XMLHttpRequest) {
            /*        if (jQuery.trim(responseText) != '') {
                CARBON.showWarningDialog(responseText);
                return;
            }*/
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["shutdown.error"]);
            } else {
                CARBON.showInfoDialog(jsi18n["shutdown.in.progress.message"]);
            }
        }
    );
}

function restartServerGracefully() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["graceful.restart.verification"],restartServerGracefullyCallback,null);
}

function restartServerGracefullyCallback() {
    jQuery.noConflict();
    jQuery.post("proxy_ajaxprocessor.jsp",
        {
            "action":"restartGracefully"
        },function (responseText, status, XMLHttpRequest) {
            /*        if (jQuery.trim(responseText) != '') {
                CARBON.showWarningDialog(responseText);
                return;
            }*/
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["graceful.restart.error"]);
            } else {
                CARBON.showInfoDialog(jsi18n["graceful.restart.in.progress.message"]);
            }
        }
    );
}

function restartServer() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["restart.verification"],restartServerCallback,null);
}

function restartServerCallback() {
    jQuery.noConflict();
    jQuery.post("proxy_ajaxprocessor.jsp",
        {
            "action":"restart"
        },function (responseText, status, XMLHttpRequest) {
            /*        if (jQuery.trim(responseText) != '') {
                CARBON.showWarningDialog(responseText);
                return;
            }*/
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["restart.error"]);
            } else {
                CARBON.showInfoDialog(jsi18n["restart.in.progress.message"]);
            }
        }
    );
}