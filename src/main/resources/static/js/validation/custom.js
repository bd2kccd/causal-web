$.validator.addMethod("nowhitespace", function (value, element) {
    return this.optional(element) || /^\S+$/i.test(value);
}, "No white space please.");
$.validator.addMethod("greaterThan", function (value, element, param) {
    return this.optional(element) || Number(value) > Number(param);
}, "Value must be greater.");
