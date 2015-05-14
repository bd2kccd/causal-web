/* 
 * Copyright (C) 2015 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

$(document).ready(function () {
    $('#login').validate({
        rules: {
            username: {
                minlength: 4,
                required: true
            },
            password: {
                minlength: 5,
                maxlength: 25,
                required: true
            }
        },
        messages: {
            username: "Please enter your username.",
            password: "Please enter your password."
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        unhighlight: function (element) {
            $(element).closest('.form-group').removeClass('has-error');
        },
        errorElement: 'span',
        errorClass: 'help-block',
        errorPlacement: function (error, element) {
            if (element.parent('.input-group').length) {
                error.insertAfter(element.parent());
            } else {
                error.insertAfter(element);
            }
        }
    });
    $('#registration').validate({
        rules: {
            username: {
                minlength: 4,
                required: true
            },
            email: {
                email: true,
                required: true
            },
            password: {
                minlength: 5,
                maxlength: 25,
                required: true
            },
            confirmPassword: {
                equalTo: "#password"
            }
        },
        messages: {
            username: "Please enter a username.",
            email: "Please enter a valid email.",
            password: "Please enter a password.",
            confirmPassword: "Please reenter password."
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        unhighlight: function (element) {
            $(element).closest('.form-group').removeClass('has-error');
        },
        errorElement: 'span',
        errorClass: 'help-block',
        errorPlacement: function (error, element) {
            if (element.parent('.input-group').length) {
                error.insertAfter(element.parent());
            } else {
                error.insertAfter(element);
            }
        }
    });
});