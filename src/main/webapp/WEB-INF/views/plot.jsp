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
        <title>CCD Demo: Plot</title>
        <meta name="description" content="" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="${path}/vendor/metisMenu/metisMenu.min.css" />
        <link rel="stylesheet" href="${path}/vendor/font-awesome/css/font-awesome.min.css" />
        <link rel="stylesheet" href="${path}/css/styles.css" />
        <link rel="stylesheet" href="${path}/css/main.css" />
        <script src="${path}/vendor/modernizr-2.8.3.min.js"></script>
        <style>
            iframe {
                width: 100%;
                height: 500px;
                border: 1px solid #DEDEDE;
            }
        </style>
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
                        <h1 class="page-header">Causal Graph</h1>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <iframe src="${link}" marginwidth="0" marginheight="0" scrolling="no"></iframe>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <p>
                            <a href="${link}" class="btn btn-info" role="button">View Full Screen</a>
                        </p>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">File: ${plot}</div>
                            <div class="panel-body">
                                <div class="table-responsive">
                                    <table class="table table-striped table-hover">
                                        <thead>
                                            <tr>
                                                <th>Attribute</th>
                                                <th>Value</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${parameters}" var="params">
                                                <tr>
                                                    <td>${params.key}</td>
                                                    <td>${params.value}</td>
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
        <script src="${path}/vendor/jquery-2.1.3.min.js"></script>
        <script src="${path}/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="${path}/vendor/metisMenu/metisMenu.min.js"></script>
        <script src="${path}/js/main.js"></script>
    </body>
</html>
