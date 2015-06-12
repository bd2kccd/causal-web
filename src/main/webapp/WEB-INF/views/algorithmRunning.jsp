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
        <title>CCD Demo: Algorithm Running</title>
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
                        <h1 class="page-header">${title}</h1>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        Please check the <a href="${path}/results">Run Result</a> page for results.
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
