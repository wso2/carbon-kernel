var plusIcon = "images/plus.gif";
var minusIcon = "images/minus.gif";
function collapseTree(imgobj) {
    var tr = imgobj.parentNode.parentNode;
    var myColspans = getNumOfColspans(tr);
    var todo = "show";
    if (imgobj.src.search(/minus.gif/) != -1) {
        todo = "hide";
    }
    if (todo == "show") {
        imgobj.src = minusIcon;
    } else {
        imgobj.src = plusIcon;
    }
    var nextTr = nextObject(tr);
    while ((parseInt(getNumOfColspans(nextTr)) < parseInt(myColspans)) && nextTr != null) {
        var imgs = null;
        var allImages = YAHOO.util.Dom.getElementsByClassName("ui-plusMinusIcon", "IMG", nextTr);
        if (allImages.length > 0) {
            imgs = allImages[0];
        }
        if (todo == "show" && (parseInt(myColspans) - parseInt(getNumOfColspans(nextTr))) == 1) {
            nextTr.style.display = "";
            if (imgs != null) {
                imgs.src = plusIcon;
            }
        } else {
            nextTr.style.display = "none";
            if (imgs != null) {
                imgs.src = minusIcon;
            }
        }
        nextTr = nextObject(nextTr);
    }
//    tr.parentNode.parentNode.id
                        
    customAlternateTableRows(tr.parentNode.parentNode.id, 'tableEvenRow', 'tableOddRow');

}

function getNumOfColspans(tr) {
    if (tr == null) {
        return 0;
    }
    var colspans = 0;
    var trChilds = tr.getElementsByTagName("td");
    for (var i = 0; i < trChilds.length; i++) {
        if (YAHOO.util.Dom.hasClass(trChilds[i], "featureNameCol")) {
            colspans = trChilds[i].getAttribute("abbr");
        }
    }
    return colspans;
}

function nextObject(obj) {
    var n = obj;
    do n = n.nextSibling;
    while (n && n.nodeType != 1);
    return n;
}

function customAlternateTableRows(id, evenStyle, oddStyle) {
    if (document.getElementsByTagName) {
        if (document.getElementById(id)) {
            var table = document.getElementById(id);
            var rowsAll = table.getElementsByTagName("tr");
            var rows = new Array();
            for (var i = 0; i < rowsAll.length; i++) {
                if (rowsAll[i].style.display != "none") {
                    rows.push(rowsAll[i]);
                }
            }
            for (i = 0; i < rows.length; i++) {
                //manipulate rows
                if (i % 2 == 0) {
                    rows[i].className = evenStyle;
                } else {
                    rows[i].className = oddStyle;
                }
            }
        }
    }
}

function selectInstallableFeatureTree(selectedCheckBox) {
    var checkBox;
    var checkBoxItems;
    var parentTr;
    var nextTr;
    var tr = selectedCheckBox.parentNode.parentNode;
    var myColspans = getNumOfColspans(tr);
    nextTr = nextObject(tr);

    while (nextTr != null && getNumOfColspans(nextTr) < myColspans) {
        checkBoxItems = YAHOO.util.Dom.getElementsByClassName("checkbox-select", "INPUT", nextTr);
        if (checkBoxItems.length > 0) {
            checkBox = checkBoxItems[0];
            checkBox.checked = selectedCheckBox.checked;
        }
        nextTr = nextObject(nextTr);
    }

    if (!selectedCheckBox.checked) {
        parentTr = getParentTR(tr);
        while (parentTr != null) {
            checkBoxItems = YAHOO.util.Dom.getElementsByClassName("checkbox-select", "INPUT", parentTr);
            if (checkBoxItems.length > 0) {
                checkBox = checkBoxItems[0];
                checkBox.checked = selectedCheckBox.checked;
            }
            parentTr = getParentTR(parentTr);
        }
    }
}

function getParentTR(obj) {
    var hiddenElements = YAHOO.util.Dom.getElementsByClassName("hidden-element-parentID", "INPUT", obj);
    var parentTrID;
    if (hiddenElements.length > 0) {
        parentTrID = hiddenElements[0].value;
        return document.getElementById(parentTrID);
    }
    return null;
}
