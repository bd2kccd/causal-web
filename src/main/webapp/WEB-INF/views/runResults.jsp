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
        <title>CCD Demo: Results</title>
        <meta name="description" content="" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="${path}/vendor/metisMenu/metisMenu.min.css" />
        <link rel="stylesheet" href="${path}/vendor/font-awesome/css/font-awesome.min.css" />
        <link rel="stylesheet" href="${path}/css/styles.css" />
        <link rel="stylesheet" href="${path}/css/main.css" />
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
                        <h1 class="page-header">Results</h1>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">Results</div>
                            <div class="panel-body">
                                <div class="table-responsive">
                                    <table class="table table-striped table-hover">
                                        <thead>
                                            <tr>
                                                <th>Filename</th>
                                                <th>Created On</th>
                                                <th>Size</th>
                                                <th>Save</th>
                                                <th>Delete</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${itemList}" var="item">
                                                <tr>
                                                    <td><a href="results/plot?file=${item.fileName}">${item.fileName}</a></td>
                                                    <td>${item.creationDate}</td>
                                                    <td>${item.size}</td>
                                                    <td><p data-placement="top" data-toggle="tooltip" title="Save"><a href="results/download?file=${item.fileName}"><button class="btn btn-success btn-xs" data-title="Save" data-toggle="modal" data-target="#Save" ><span class="glyphicon glyphicon-save"></span></button></a></p></td>
                                                    <td><p data-placement="top" data-toggle="tooltip" title="Delete"><button class="btn btn-danger btn-xs" data-title="Delete" data-toggle="modal" data-target="#confirm-delete" data-href="results/delete?file=${item.fileName}"><span class="glyphicon glyphicon-trash"></span></button></p></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <%@include file="includes/footer.jspf" %>
            </div>
        </div>
        <div class="modal fade" id="confirm-delete" tabindex="-1" role="dialog" aria-labelledby="delete" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>
                        <h4 class="modal-title custom_align" id="Heading">Delete Result</h4>
                    </div>
                    <div class="modal-body">
                        <div class="alert alert-danger"><span class="glyphicon glyphicon-warning-sign"></span> Are you sure you want to delete this result?</div>
                    </div>
                    <div class="modal-footer ">
                        <a class="btn-ok"><button type="button" class="btn btn-danger" ><span class="glyphicon glyphicon-ok-sign"></span> Yes</button></a>
                        <button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> No</button>
                    </div>
                </div>
                <!-- /.modal-content --> 
            </div>
            <!-- /.modal-dialog --> 
        </div>
        <script src="${path}/vendor/jquery-2.1.3.min.js"></script>
        <script src="${path}/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="${path}/vendor/metisMenu/metisMenu.min.js"></script>
        <script src="${path}/js/main.js"></script>
        <script src="${path}/js/runResults.js"></script>
    </body>
</html>
