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
    $('#userSetup').validate({
        rules: {
            firstName: {
            	rangelength: [2,24],
                required: true
            },
            lastName: {
            	rangelength: [2,24],
                required: true
            },
            email: {
                email: true,
                required: true
            },
            workspaceDirectory: {
                minlength: 1,
                required: true
            }
        },
        messages: {
            firstName: "Please enter your first name.",
            lastName: "Please enter your last name.",
            email: "Please enter a valid email.",
            workspaceDirectory: "Please enter an existing directory for the workspace."
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
    
    //Auto hint functions
    $('INPUT.auto-hint, TEXTAREA.auto-hint').focus(function(){
        if($(this).val() == $(this).attr('title')){
            $(this).val('');
            $(this).removeClass('auto-hint');
        }
    });
    
    $('INPUT.auto-hint, TEXTAREA.auto-hint').blur(function(){
        if($(this).val() == '' && $(this).attr('title') != ''){
           $(this).val($(this).attr('title'));
           $(this).addClass('auto-hint');
        }
    });

    $('INPUT.auto-hint, TEXTAREA.auto-hint').each(function(){
        if($(this).attr('title') == ''){ return; }
        if($(this).val() == ''){ $(this).val($(this).attr('title')); }
        else { $(this).removeClass('auto-hint'); }
    });
    
    $('#firstName').attr('title','First name is a name that your mother calls you i.e. John.');
});
