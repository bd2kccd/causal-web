<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="path" value="${pageContext.request.contextPath}" />
<!doctype html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7" lang=""> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8" lang=""> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9" lang=""> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js" lang=""> <!--<![endif]-->
    <head>
        <meta charset="utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
        <title>CCD Demo: Upload Dataset</title>
        <meta name="description" content="" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="${path}/vendor/metisMenu/metisMenu.min.css" />
        <link rel="stylesheet" href="${path}/vendor/font-awesome/css/font-awesome.min.css" />
        <link rel="stylesheet" href="${path}/css/styles.css" />
        <link rel="stylesheet" href="${path}/css/main.css" />
        <link rel="stylesheet" href="${path}/css/dataUpload.css" />
        <script src="${path}/vendor/modernizr-2.8.3.min.js"></script>
    </head>
    <body>
        <div id="wrapper">
            <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
                <%@include file="includes/navbar.jspf" %>
                <%@include file="includes/sidebar.jspf" %>
            </nav>
            <div id="page-wrapper">
                <div class="row">
                    <div class="col-lg-12">
                        <h1 class="page-header">Data Upload</h1>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <div class="resumable-error">Your browser, unfortunately, is not supported by Resumable.js. The library requires support for <a href="http://www.w3.org/TR/FileAPI/">the HTML5 File API</a> along with <a href="http://www.w3.org/TR/FileAPI/#normalization-of-params">file slicing</a>.</div>
                        <div class="resumable-drop" ondragenter="jQuery(this).addClass('resumable-dragover');" ondragleave="jQuery(this).removeClass('resumable-dragover');" ondrop="jQuery(this).removeClass('resumable-dragover');">
                            Drop file here or <div class="btn btn-primary btn-file resumable-browse"><span class="glyphicon glyphicon-folder-open"></span>&nbsp;&nbsp;&nbsp;Browse</div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <div class="resumable-list">
                            <div class="panel panel-primary">
                                <div class="panel-heading">
                                    <h3 class="panel-title">Upload Big Data</h3>
                                </div>
                                <div class="panel-body">
                                    <table class="table table-striped table-bordered file-info">
                                        <thead>
                                            <tr>
                                                <th>Name</th>
                                                <th>Size</th>
                                                <th>Last Modified</th>
                                                <th>MD5 Checksum</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <td colspan="4">
                                                    <div class="resumable-progress">
                                                        <div class="progress progress-striped progress-container"><div class="bar progress-bar"></div></div>
                                                        <span class="progress-text" style="white-space: nowrap"></span>
                                                        <div class="progress-pause" style="white-space: nowrap">
                                                            <a href="#" onclick="r.upload();
                                                                    return(false);" class="progress-resume-link"><span class="glyphicon glyphicon-play" aria-hidden="true"></span></a>
                                                            <a href="#" onclick="r.pause();
                                                                    return(false);" class="progress-pause-link"><span class="glyphicon glyphicon-pause" aria-hidden="true"></span></a>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <%@include file="includes/footer.jspf" %>
            </div>
        </div>
        <script src="${path}/vendor/jquery-2.1.3.min.js"></script>
        <script src="${path}/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="${path}/vendor/metisMenu/metisMenu.min.js"></script>
        <script src="${path}/js/main.js"></script>
        <script src="${path}/vendor/resumable.js"></script>
        <script src="${path}/js/dataUpload.js"></script>
    </body>
</html>
