/*all validation functions required by the .jsp files*/

function validateEmpty(fldname) {
    var fld = document.getElementsByName(fldname)[0];
    var error = "";
    var value = fld.value;
    if (value.length == 0) {
        error = fld.name+" ";
        return error;
    }
    
    value = value.replace(/^\s+/, "") ;
    if (value.length == 0) {
        error = fld.name + "(contains only spaces) ";
        return error;  
    }
    
    return error;  
}

function validateName(fldname) {
    var invalid = " "; // Invalid character is a space
    var fld = document.getElementsByName(fldname)[0];
    var error = "";
    var value = fld.value;
    
    if (value.indexOf(invalid) > -1) {
        error = fld.name;
    }
    return error;
}

function isAtleastOneCheckedIfExisting(fldname){
    var foundOne = false;
    var elems = document.getElementsByName(fldname);
    
    if(elems.length == 0){
        foundOne = true;
    }else{
        var counter=0;
        for (counter=0; counter < elems.length; counter++) {
            if(elems[counter].checked == true) 
                foundOne= true;
        }
    }
    return foundOne;
}

function isAtleastOneChecked(fldname){
    var foundOne = null;
    var elems = document.getElementsByName(fldname);
    
    var counter=0;
    for (counter=0; counter < elems.length; counter++) {
        if(elems[counter].checked == true)
            foundOne= elems[counter].value;
    }
    return foundOne;
}

function validatePassword(fld1name , fld2name){
    var error = "";
    var invalid = " "; // Invalid character is a space
    var minLength = 6; // Minimum length
    var pw1 = document.getElementsByName(fld1name)[0].value;
    var pw2 = document.getElementsByName(fld2name)[0].value;
    // check for a value in both fields.
    if (pw1 == '' || pw2 == '') {
        error = 'Please enter the password twice.';
        return error;
    }
    
    // check for minimum length
    if (pw1.length < minLength) {
        error = 'Your password must be at least ' + minLength + ' characters long. Try again.';
        return error;
    }

    // check for spaces
    if (pw1.indexOf(invalid) > -1) {
        error = "Sorry, spaces are not allowed.";
        return error;
    }
    
    
    if (pw1 != pw2) {
        error = "You did not enter the same password twice. Please re-enter your password.";
        return error;
    }
    
    return error;
}
