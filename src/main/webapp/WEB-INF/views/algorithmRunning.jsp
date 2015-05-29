<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="path" value="${pageContext.request.contextPath}" />
<jsp:useBean id="date" class="java.util.Date" />
<!doctype html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7" lang="en"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8" lang="en"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9" lang="en"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js" lang="en"> <!--<![endif]-->
    <head>
        <meta charset="utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
        <title>CCD: Algorithm Running</title>
        <meta name="description" content="" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="${path}/vendor/metismenu/metisMenu.min.css" />
        <link rel="stylesheet" href="${path}/vendor/font_awesome/css/font-awesome.min.css" />
        <link rel="stylesheet" href="${path}/vendor/admin/css/sb-admin-2.css" />
        <link rel="stylesheet" href="${path}/css/styles.css" />
    </head>
    <body>
        <div id="wrapper">
            <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="${path}/home">
                    	${ccd.title}
                    </a>
                </div>
                <%@include file="includes/navbar.jspf"%>
                <%@include file="includes/sidebar.jspf"%>
            </nav>
            <div id="page-wrapper">
            
                <div class="row">
                    <div class="col-lg-12">
                        <h2 class="page-header">${title}</h2>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        Please check the <a href="${path}/results">Run Result</a> page for results.
                    </div>
                </div>

                <div class="row">
                	&nbsp;
                </div>
                
                <%@include file="includes/footer.jspf" %>

            </div>
        </div> 
        
        <script src="${path}/vendor/jquery/jquery-2.1.3.min.js"></script>
        <script src="${path}/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="${path}/vendor/metismenu/metisMenu.min.js"></script>
        <script src="${path}/vendor/admin/js/sb-admin-2.js"></script>
        <script src="${path}/js/stepwizard.js"></script>
        <script src="${path}/js/algorithm.js"></script>
    </body>
</html>
