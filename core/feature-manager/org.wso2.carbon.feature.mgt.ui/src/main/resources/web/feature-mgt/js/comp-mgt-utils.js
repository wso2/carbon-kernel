var previouslySelectedRowColor;
var previouslySelectedRow;
var preveouslySelectedTableID;

var selectedRepoURL;
var updatedStatus;
var selectedRepoNickName;

var isSettingsChanged = false;

var tab01ActiveDivID;
var tab02ActiveDivID;
var tab03ActiveDivID;

var allowTabChange = true;

var addRepositoryOnCompleteReturnTabID;

var $myTabs;

$(function() {
    $myTabs = $("#tabs");

    $myTabs.tabs({
                select: function(event, ui) {
//                    if (!allowTabChange) {
////                        alert("Tab selection is disabled, while you are in the middle of a workflow");
//                    }
                    return allowTabChange;
                },

        show: function(event, ui) {
            var selectedTab = $myTabs.tabs('option', 'selected');
            if (selectedTab == '0') {
                //reloading the repositories combo-box
                loadSearchFeaturesDiv();
            } else if (selectedTab == "1") {
                //reload installed feature main page
                 loadInstalledFeatureTable();
                if (isSettingsChanged) {
                    swapVisiblility(tab02ActiveDivID, "_div_tabs02-IF", 2);
                    loadInstalledFeatureTable();
                }
            } else if (selectedTab == '2'){
                loadProfileHistory();
            }
        }
    });

    //Select a tab
    $myTabs.tabs('select', 0);

    //How to disable a tab
    //$myTabs.tabs('disable', 1);

    //How to get the selected tab
    //var tab_num = $myTabs.tabs('option', 'selected');

    $('#something').click(function() {
        var tab_num = $myTabs.tabs('option', 'selected');
        //var nextTab = tab_num + 1;
        //$myTabs.tabs('select', nextTab);
        doNext('AF');
    });

    $('#something_back').click(function() {
        //var tab_num = $myTabs.tabs('option', 'selected');
        //alert(tab_num);
        //var nextTab = tab_num + 1;
        //$myTabs.tabs('select', nextTab);
        doBack('AF_RF');
    });
});

function loadFeatures() {
    sessionAwareFunction(function() {
        var repoURL = document.getElementById('_select_repositoryCombo').value;
        var searchStr = document.getElementById('_txt_AF_filterString').value;
        var query = 'searchQuery';
        var groupByCategory = 'false';
        var hideInstalled = 'true';
        var showLatest = 'false';
        if (document.getElementById('_chk_show_latest').checked) {
            showLatest = 'true';
        }
        if (document.getElementById('_chk_groupBy_category').checked) {
        	groupByCategory = 'true';
        }
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_tabs01-step-01-AF").load('available_features-ajaxprocessor.jsp',
        {   queryType: query,
            repoURL: repoURL,
            groupByCategory: groupByCategory,
            hideInstalled: hideInstalled,
            showLatest: showLatest,
            filterStr: searchStr
        },
                function (responseText, status, XMLHttpRequest) {
                    CARBON.closeWindow();
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        makeElementVisible("_div_tabs01-step-01-AF");
                        registerTooltips('span.featureDiscriptionOfAvailableFeatures', 'false');
                    }
                });
    });
}

function registerTooltips(cssSelector , isInstalledFeature){
 jQuery(cssSelector).each(function(idx, el) {
                            var featureId=jQuery(el).attr("id").toString();
                            jQuery(el).attr("rel","feature_description-ajaxprocessor.jsp").cluetip({
                                showTitle: false,
                                width: 500,
                                hoverIntent: {
                                    sensitivity:  3,
                                    interval:     500,
                                    timeout:      750
                                },
                                fx: {
                                    open:       'slideDown', // can be 'show' or 'slideDown' or 'fadeIn'
                                    openSpeed:  ''
                                },
                                ajaxSettings:{
                                  type:"POST",
                                  data:"featureId="+featureId+"&isInstalledFeature="+isInstalledFeature

                                },
                                ajaxCache: true,
                                positionBy: 'bottomTop'
                            });
                        });
}

function searchAvailableFeatures() {
    sessionAwareFunction(function() {
        var filterString = document.getElementById('_txt_AF_filterString').value;
        var repoURL = document.getElementById('_select_repositoryCombo').value;
        var groupByCategory = 'false';
        var showLatest = 'false';
        if (document.getElementById('_chk_show_latest').checked) {
            showLatest = 'true';
        }
        if (document.getElementById('_chk_groupBy_category').checked) {
        	groupByCategory = 'true';
        }
        
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_tabs01-step-01-AF").load('available_features-ajaxprocessor.jsp',
        {   queryType: 'searchQuery',
        	repoURL: repoURL,
            groupByCategory: groupByCategory,
            showLatest: showLatest,            
            filterStr: filterString
        },
                function (responseText, status, XMLHttpRequest) {
                    CARBON.closeWindow();
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);
                    }
                });
     });
}

function searchInstalledFeatures() {
    sessionAwareFunction(function() {
        var filterString = document.getElementById('_txt_IF_filterString').value;
        var featureType= document.getElementById('_select_feature_type_top').value;

        if (featureType != "ALL") {
            filterString = "";
        }
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_installed_features_list").load('installed_features-ajaxprocessor.jsp',
        {   queryType: 'searchQuery',
            filterType: featureType,
            filterStr: filterString
        },
                function (responseText, status, XMLHttpRequest) {
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);
                    }else {
                        makeElementVisible("_div_installed_features_list");
                        registerTooltips('span.featureDiscriptionOfInstalledFeatures', 'true');
                    }
                });
     });
}

function reviewInstallationPlan(selectedFeatures) {
    sessionAwareFunction(function() {
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_tabs01-step-02-RF").load('review_install_action-ajaxprocessor.jsp',
        {
            selectedFeatures: selectedFeatures
        },
        function (responseText, status, XMLHttpRequest) {
            wso2.wsf.Util.cursorClear();
            if (status != "success") {
                CARBON.showErrorDialog("Backend Service is Unavailable");

            } else if (responseText.search('compMgtErrorMsg') != -1) {
                CARBON.showErrorDialog(responseText);

            } else {
                swapVisiblility("_div_tabs01-step-00-FR", "_div_tabs01-step-02-RF", 1);
            }
        });
    });
}

function loadLicenseInformation() {
    sessionAwareFunction(function() {
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_tabs01-step-03-RL").load('display_licenses-ajaxprocessor.jsp',
            {
            },

            function (responseText, status, XMLHttpRequest) {
                wso2.wsf.Util.cursorClear();
                if (status != "success") {
                    CARBON.showErrorDialog("Backend Service is Unavailable");

                } else if (responseText.search('compMgtErrorMsg') != -1) {
                    CARBON.showErrorDialog(responseText);

                } else {
                    swapVisiblility("_div_tabs01-step-02-RF", "_div_tabs01-step-03-RL", 1);
                }
            });
    });
}

function performInstallation(actionType) {
    sessionAwareFunction(function() {
        allowTabChange = false;
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery.post('perform_installation-ajaxprocessor.jsp',
            {
                actionType: actionType
            },
            function (responseText, status, XMLHttpRequest) {
                allowTabChange = true;
                wso2.wsf.Util.cursorClear();
                if (status != "success") {
                    CARBON.showErrorDialog("Backend Service is Unavailable");
            
                } else if (responseText.search('compMgtErrorMsg') != -1) {
                    CARBON.showErrorDialog(responseText);

                } else {
                    swapVisiblility("_div_tabs01-step-04-INSTALLING", "_div_tabs01-step-05-IC", 1);
                }
            });
    });
}

function reviewUninstallationPlan(selectedFeatures) {
    sessionAwareFunction(function() {
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_tabs02-RUF").load('review_uninstall_action-ajaxprocessor.jsp',
        {
            selectedFeatures: selectedFeatures
        },

                function (responseText, status, XMLHttpRequest) {
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility("_div_tabs02-IF", "_div_tabs02-RUF", 2);
                    }
                });
     });
}

function reviewUninstallationFeaturesWithPropertiesPlan(featurePropery, featureValue) {
    sessionAwareFunction(function() {
        swapVisiblility("_div_tabs02-IF", "_div_tabs02-UNINSTALLING", 2);
        allowTabChange = false;
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_tabs02-RUF").load('review_uninstall_action-ajaxprocessor.jsp',
        {
            featurePropery: featurePropery,
            featureValue: featureValue
        },

                function (responseText, status, XMLHttpRequest) {
                    allowTabChange = true;                    
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility("_div_tabs02-UNINSTALLING", "_div_tabs02-UC", 2);
                    }
                });
     });
}

function performUninstallation(actionType) {
    sessionAwareFunction(function() {
        allowTabChange = false;        
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery.post('perform_installation-ajaxprocessor.jsp',
        {
            actionType:actionType
        },
                function (responseText, status, XMLHttpRequest) {
                    allowTabChange = true;                            
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility("_div_tabs02-RUF", "_div_tabs02-UC", 2);
                    }
                });
     });
}

function loadProfileHistory(){
    if(document.getElementById("_div_tabs03-IH").innerHTML == ""){
        sessionAwareFunction(function() {
            allowTabChange = false;
            wso2.wsf.Util.cursorWait();
            jQuery.noConflict();
            jQuery("#_div_tabs03-IH").load('installation_history-ajaxprocessor.jsp',
            {
            },
                function (responseText, status, XMLHttpRequest) {
                    allowTabChange = true;
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility("_div_tabs03-Loading-IH", "_div_tabs03-IH", 3);
                    }
                });
        });
    }
}

function loadInstalledFeatureTable(force){
    if(force || document.getElementById("_div_installed_features_list").innerHTML ==""){
        sessionAwareFunction(function(){
           allowTabChange = false;
            wso2.wsf.Util.cursorWait();
            jQuery.noConflict();
            jQuery("#_div_installed_features_list").load('installed_features-ajaxprocessor.jsp',
            {
                queryType: "searchQuery"  ,
                filterStr: ""       
            },
                function (responseText, status, XMLHttpRequest) {
                    allowTabChange = true;
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility("_div_tabs02_loading_IF", "_div_installed_features_list", 2);
                        registerTooltips('span.featureDiscriptionOfInstalledFeatures', 'true');
                    }
                }); 
        });

    }
}

function performRevertOperation(actionType) {
    sessionAwareFunction(function() {
        allowTabChange = false;
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery.post('perform_installation-ajaxprocessor.jsp',
        {
            actionType:actionType
        },
                function (responseText, status, XMLHttpRequest) {
                    allowTabChange = true;                            
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility("_div_tabs03-RP", "_div_tabs03-RC", 3);
                    }
                });
    });
}

function loadSelectedFeatureDetails(featureID, featureVersion, isInstalledFeature, divIDToShow, divIDToHide, tabNumber){
    sessionAwareFunction(function() {
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#" + divIDToShow).load('get_installed_feature_details-ajaxprocessor.jsp',
        {
            featureID:featureID,
            featureVersion:featureVersion,
            isInstalledFeature:isInstalledFeature,
            divIDToShow:divIDToShow,
            divIDToHide:divIDToHide,
            tabNumber:tabNumber
        },
                function (responseText, status, XMLHttpRequest) {
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility(divIDToHide, divIDToShow, tabNumber);
                    }
                });
    });
}

function loadSearchFeaturesDiv() {
    sessionAwareFunction(function() {
        if (isSettingsChanged) {
            isSettingsChanged = false;

            wso2.wsf.Util.cursorWait();
            jQuery.noConflict();
            jQuery("#_div_tabs01-step-00-FR").load('search_features-ajaxprocessor.jsp',
            {},
                    function (responseText, status, XMLHttpRequest) {
                        wso2.wsf.Util.cursorClear();
                        if (status != "success") {
                            CARBON.showErrorDialog("Backend Service is Unavailable");

                        } else if (responseText.search('compMgtErrorMsg') != -1) {
                            CARBON.showErrorDialog(responseText);

                        } else {
                            swapVisiblility(tab01ActiveDivID, '_div_tabs01-step-00-FR', 1);
                        }
                    });
        }
    });
}

function loadRepositories() {
    sessionAwareFunction(function() {
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_repository_list").load('manage_repositories-ajaxprocessor.jsp',
        {},
                function (responseText, status, XMLHttpRequest) {
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    }
                    /*else {*/
//                        swapVisiblility('_div_tabs04-MAIN', '_div_tabs04-MR', 4);
//                    }
                });
    });
}

function isValidURL(url, element) {
    // Here we are not handling the no url case
    if (url.length == 0) {
        return true;
    }

    // if user has not entered http:// https:// or ftp:// assume they mean http://
    if(!/^(http|https|ftp):\/\//i.test(url)) {
        url = 'http://'+url; // set both the value
        $(element).val(url); // also update the form element
    }
    // now check if valid url
    return /^(http|https|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(url);
}

function validateAddRepositoryInputs(){

    // chk weather the url repository is selected
   if(document.getElementById('_chk_repository_location_url').checked){

       var isValid=isValidURL(document.getElementById('_txt_repository_location_url').value,document.getElementById('_txt_repository_location_url'));
       if(!isValid){
           CARBON.showErrorDialog("The Given URL is not valid");
           return isValid;

       }else{
           return true;
       }

   }else if(document.getElementById('_chk_repository_location_file').checked){
       //right now we are not doing any checks on local repositoy path
       return true;
   }

    

}

function addRepository(displayMsg) {
    sessionAwareFunction(function() {
        if(validateAddRepositoryInputs()){
        CARBON.showInfoDialog(displayMsg);

        var nickName = document.getElementById('_txt_repository_name').value;
        var repoURL;
        var local;
        if(document.getElementById('_chk_repository_location_url').checked){
            local = false;            
            repoURL = document.getElementById('_txt_repository_location_url').value;
        } else {
            local = true;
            repoURL = document.getElementById('_txt_repository_location_file').value;
        }

        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery.post('update_repository-ajaxprocessor.jsp',
        {   action: 'addRepo',
            repoURL: repoURL,
            nickName:nickName,
            local:local
        },

                function (responseText, status, XMLHttpRequest) {
                    CARBON.closeWindow();
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        isSettingsChanged = true;
                        if(addRepositoryOnCompleteReturnTabID == "AF"){
                            $myTabs.tabs('select', 0);                            
                            loadRepositories();
                            swapVisiblility('_div_tabs04-AR', '_div_tabs04-MR', 4);
                        } else {
                            swapVisiblility('_div_tabs04-AR', '_div_tabs04-MR', 4);
                            loadRepositories();
                        }
                    }
                });
        }
    });
}

function editRepository() {
    sessionAwareFunction(function() {        
        var updatedNickName = document.getElementById('_txt_edit_repository_name').value;
        var updatedRepoURL = document.getElementById('_txt_edit_repository_location').value;

        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery.post('update_repository-ajaxprocessor.jsp',
        {   action: 'editRepo',
            repoURL: selectedRepoURL,
            nickName: selectedRepoNickName,
            updatedRepoURL: updatedRepoURL,
            updatedNickName: updatedNickName
        },
                function (responseText, status, XMLHttpRequest) {
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        isSettingsChanged = true;
                        swapVisiblility('_div_tabs04-ER', '_div_tabs04-MR', 4);
                        loadRepositories();
                    }
                });
    });
}

function removeRepository() {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        jQuery.post('update_repository-ajaxprocessor.jsp',
        {   action: 'removeRepo',
            repoURL: selectedRepoURL
        },

                function (responseText, status, XMLHttpRequest) {
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        isSettingsChanged = true;
                        loadRepositories();
                    }
                });
    });
}

function enableRepository() {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        jQuery.post('update_repository-ajaxprocessor.jsp',
        {   action: 'enableRepo',
            repoURL: selectedRepoURL,
            enabled: updatedStatus
        },
                function (responseText, status, XMLHttpRequest) {
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        isSettingsChanged = true;
                        loadRepositories();
                    }
                });
    });
}

function getRevertPlan(timestamp){
    sessionAwareFunction(function() {
        wso2.wsf.Util.cursorWait();
        jQuery.noConflict();
        jQuery("#_div_tabs03-RP").load('revert_plan-ajaxprocessor.jsp',
        {   timestamp: timestamp
        },
                function (responseText, status, XMLHttpRequest) {
                    wso2.wsf.Util.cursorClear();
                    if (status != "success") {
                        CARBON.showErrorDialog("Backend Service is Unavailable");

                    } else if (responseText.search('compMgtErrorMsg') != -1) {
                        CARBON.showErrorDialog(responseText);

                    } else {
                        swapVisiblility('_div_tabs03-IH', '_div_tabs03-RP', 3);
                    }
                }); 
    });
}

function clearTableRowSelection() {
    previouslySelectedRow.style.backgroundColor = previouslySelectedRowColor;
    previouslySelectedRow = undefined;
    preveouslySelectedTableID = undefined;
}

function setSelectedRepoProperties(url, nickName, status) {
    selectedRepoURL = url;
    if (nickName != null) {
        selectedRepoNickName = nickName;
    }
    if (status != null) {
        updatedStatus = status;
    }
}

function fillEditRepoTextBoxes() {
    document.getElementById('_txt_edit_repository_name').value = selectedRepoNickName;
    document.getElementById('_txt_edit_repository_location').value = selectedRepoURL;

}

function showConfirmationDialogBox(message, yesCallback) {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(message, yesCallback, null);
}

function isFeaturesSelected(elementName){
    var featureElements = document.getElementsByName(elementName);
    for (var j = 0; j < featureElements.length; j++) {
        if (featureElements[j].checked) {
            return true;
        }
    }
    return false;
}

function getSelectedFeatureList(elementName) {
    var checkBoxItems;
    var featureElements = document.getElementsByName(elementName);
    var selectedFeatureList = new Array();
    var index = 0;
    for (var j = 0; j < featureElements.length; j++) {
        if (!featureElements[j].disabled && featureElements[j].checked){
            var childTr = featureElements[j].parentNode.parentNode;
            var childTrChkBox = featureElements[j];
//            var parentTr = getParentTR(childTr);
//            while (parentTr != null) {
//                checkBoxItems = YAHOO.util.Dom.getElementsByClassName("checkbox-select", "INPUT", parentTr);
//                if (checkBoxItems.length > 0 && checkBoxItems[0].checked) {
//                    childTr = parentTr;
//                    childTrChkBox = checkBoxItems[0]
//                    parentTr = getParentTR(childTr);
//                } else {
//                    break;
//                }
//            }
            selectedFeatureList[index] = childTrChkBox.value;
            index = index + 1;
        }
    }
    return selectedFeatureList;
}

function selecteAllAvailableFeatures(check) {
    var featureElements = document.getElementsByName('chkSelectFeaturesToInstall');
    for(var i = 0; i < featureElements.length; i++){
        if(check){
            featureElements[i].checked = true;

        } else {
            featureElements[i].checked = false;

        }
    }
}

function selecteAllUninstallableFeatures(check) {
    var featureElements = document.getElementsByName('chkSelectFeaturesToUninstall');
    for(var i = 0; i < featureElements.length; i++){
    	if(check && featureElements[i].disabled == false){
        	featureElements[i].checked = true;
        	
        } else {
            featureElements[i].checked = false;

        }
    }
}
function selecteAllUninstallableFeaturesInAllPages(){
    selecteAllUninstallableFeatures(true);
    allServiceGroupsSelected = true;
    return false;
}

function swapVisiblility(idToHide, idToShow, tabNumber) {
    makeElementInvisible(idToHide);
    makeElementVisible(idToShow);

    if (tabNumber == 1) {
        tab01ActiveDivID = idToShow;

    } else if (tabNumber == 2) {
        tab02ActiveDivID = idToShow;

    } else if (tabNumber == 3) {
        tab03ActiveDivID = idToShow;
    }
}

function makeElementVisible(id) {
    if(document.getElementById(id) != null){
    	document.getElementById(id).style.display = "inline";
    }
}

function makeElementInvisible(id) {
     if(document.getElementById(id) != null){
    	document.getElementById(id).style.display = "none";
    }
}


function doNext(context) {
    var selectedFeatures = undefined;

    if (context == "FR-AF") {
        loadFeatures();

    } else if (context == 'AF-RF') {
        selectedFeatures = getSelectedFeatureList("chkSelectFeaturesToInstall");
        if (selectedFeatures.length == 0) {
            CARBON.showWarningDialog("Please Select features to be installed.");
        } else {
            reviewInstallationPlan(selectedFeatures);
        }

    } else if (context == 'RF-RL') {
        loadLicenseInformation();

    } else if (context == 'UF-RUF') {
        selectedFeatures = getSelectedFeatureList("chkSelectFeaturesToUninstall");
        if (selectedFeatures.length == 0) {
            CARBON.showWarningDialog("Please Select features to be uninstalled.");
        } else {
            reviewUninstallationPlan(selectedFeatures);
        }

    } else if (context == "AU") {
        document.getElementById("_div_tabs02-step-01").style.display = "none";
        document.getElementById("_div_tabs02-step-02").style.display = "inline";
    }
}

function doBack(context) {

     if (context == "RF-AF") {
        swapVisiblility("_div_tabs01-step-02-RF", "_div_tabs01-step-00-FR", 1);

    } else if (context == 'RL-RF') {
        swapVisiblility("_div_tabs01-step-03-RL", "_div_tabs01-step-02-RF", 1);

    } else  if (context == "RL-AF") {
        swapVisiblility("_div_tabs01-step-03-RL", "_div_tabs01-step-00-FR", 1);

    } else if(context == "IC-AF"){
        swapVisiblility("_div_tabs01-step-05-IC", "_div_tabs01-step-00-FR", 1);

    } else if (context == "RUF-IF") {
        swapVisiblility("_div_tabs02-RUF", "_div_tabs02-IF", 2);

    } else if (context == "UC-IF") {
        swapVisiblility("_div_tabs02-UC", "_div_tabs02-IF", 2);

    } else if (context == "MR-MAIN") {
        swapVisiblility("_div_tabs04-MR", "_div_tabs04-MAIN", 4);

    } else if (context == "AR-MR") {
        if(addRepositoryOnCompleteReturnTabID == "AF"){
            $myTabs.tabs('select', 0);
        }
        swapVisiblility("_div_tabs04-AR", "_div_tabs04-MR", 4);

    } else if (context == "ER-MR") {
        swapVisiblility("_div_tabs04-ER", "_div_tabs04-MR", 4);
         
    } else if (context == "RP-IH"){
        swapVisiblility('_div_tabs03-RP', '_div_tabs03-IH', 3);
         
    } else if (context == "RC-IH"){
        swapVisiblility('_div_tabs03-RC', '_div_tabs03-IH', 3);            
    }
}

function doCancel() {

}

function doFinish(context) {
    var actionType;
    if (context == "IF") {
        actionType = document.getElementById("_hidden_RL_actionType").value;
        swapVisiblility('_div_tabs01-step-03-RL', '_div_tabs01-step-04-INSTALLING', 1);
        performInstallation(actionType);

    } else if (context == "UF") {
        actionType = document.getElementById("_hidden_UF_actionType").value;
        performUninstallation(actionType);
    }
}


function doUninstall() {
    var featureType = document.getElementById('_select_feature_type_top').value;
    var selectedFeatures = getSelectedFeatureList("chkSelectFeaturesToUninstall");
    if (featureType == "ALL") {
    if (selectedFeatures.length == 0) {
            CARBON.showWarningDialog("No features found to be uninstalled.");
    } else {
        reviewUninstallationPlan(selectedFeatures);
    }
    } else if (featureType == "FRONT_END") {
        reviewUninstallationFeaturesWithPropertiesPlan("org.wso2.carbon.p2.category.type", "console");
    } else if (featureType == "BACK_END") {
        reviewUninstallationFeaturesWithPropertiesPlan("org.wso2.carbon.p2.category.type", "server");
}
}

function doRevert(){
    var actionType = document.getElementById("_hidden_RP_actionType").value;
    performRevertOperation(actionType);
}

function doAcceptLicenses() {
//    if (context == "AF") {
        document.getElementById("_btn_finish_af_review_licenses").disabled = false;
//    } else if (context == "AU") {
//        document.getElementById("_btn_finish_au_review_licenses").disabled = false;
//    }
}

function doDeclineLicenses() {
//    if (context == "AF") {
        document.getElementById("_btn_finish_af_review_licenses").disabled = true;

//    } else if (context == "AU") {
//        document.getElementById("_btn_finish_au_review_licenses").disabled = true;
//    }
}

function checkBoxSelectedUninstall(chkBox) {
    selectInstallableFeatureTree(chkBox);


}

function checkBoxSelectedinstall(chkBox) {
    selectInstallableFeatureTree(chkBox);
}
    
function featureTypeOnChange(optionBox) {
    if (optionBox.selectedIndex == "2") {// FRONT_END = 2
        disableInstalledFeatureSearch();
        searchInstalledFeatures();
    
    } else if (optionBox.selectedIndex == "1") { // BACK_END = 1
        disableInstalledFeatureSearch();
        searchInstalledFeatures();

    } else if (optionBox.selectedIndex == "0") { // ALL = 0
        enableInstalledFeatureSearch();
        searchInstalledFeatures();
}
}

function disableInstalledFeatureSearch() {
    document.getElementById("_txt_IF_filterString").disabled = true;
    document.getElementById("_icon_IF_filterString").disabled = true;
}

function enableInstalledFeatureSearch() {
    document.getElementById("_txt_IF_filterString").disabled = false;
    document.getElementById("_icon_IF_filterString").disabled = false;
}

function submitenter(e, installed) {
    var keycode;
    if (window.event) {
        keycode = window.event.keyCode;
    } else if (e) {
        keycode = e.which;
    }

    if (keycode == 13){
        if(installed){
            searchInstalledFeatures();
        } else {
            searchAvailableFeatures();            
        }
        return true;
    } else {
        return true;
    }
}

function restartServerGracefully() {
    jQuery.noConflict();
    CARBON.showConfirmationDialog(jsi18n["graceful.restart.verification"],restartServerGracefullyCallback,null);
}

function restartServerGracefullyCallback() {
    var url = "../server-admin/proxy_ajaxprocessor.jsp?action=restartGracefully";
    jQuery.noConflict();
    jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
        /*if (jQuery.trim(responseText) != '') {
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