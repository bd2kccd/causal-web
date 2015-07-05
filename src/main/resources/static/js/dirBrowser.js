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

var selectedFolder = $('#dir').val();

$(document).ready(function () {
    $('tr.table-data-row').click(function () {
        $('tr.table-data-row').removeClass('success');
        $(this).addClass('success');
        var id = $(this).attr('id');
        selectedFolder = id;
        $('#selectedDir').val(selectedFolder);
    }).dblclick(function () {
        var id = $(this).attr('id');
        window.location = 'dirBrowser?dir='.concat(id);
    });
});