<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
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
        <title>CCD: User Profile</title>
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
                    <a class="navbar-brand" href="${path}/home">${ccd.title}</a>
                </div>
                <%@include file="includes/navbar.jspf"%>
                <%@include file="includes/sidebar.jspf"%>
            </nav>
            <div id="page-wrapper">
                <div class="row">
                    <div class="col-lg-12">
                        <h1 class="page-header">User Profile</h1>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <form:form method="POST" action="" modelAttribute="person" role="form" >
                            <div class="panel panel-default" id="view_info">
                                <div class="panel-heading clearfix">
                                    <div class="pull-left">
                                        <h4><i class="fa fa-user fa-fw"></i> User Profile</h4>
                                    </div>
                                    <div class="pull-right">
                                        <div class="btn-group">
                                            <button type="button" class="btn btn-default" id="edit">
                                                <span class="glyphicon glyphicon-edit"></span> Edit
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="panel-body">
                                    <div class="row">
                                        <div class="col-sm-12">

                                            <table class="table table-striped table-hover table-responsive">
                                                <tbody>
                                                    <tr>
                                                        <td><b>First Name:</b></td>
                                                        <td>${person.firstName}</td>
                                                    </tr>
                                                    <tr>
                                                        <td><b>Last Name:</b></td>
                                                        <td>${person.lastName}</td>
                                                    </tr>
                                                    <tr>
                                                        <td><b>E-mail:</b></td>
                                                        <td>${person.email}</td>
                                                    </tr>
                                                    <tr>
                                                        <td class="col-sm-3"><b>Workspace Directory:</b></td>
                                                        <td>${person.workspaceDirectory}</td>
                                                    </tr>
                                                </tbody>
                                            </table>

                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="panel panel-default" id="edit_info">
                                <div class="panel-heading clearfix">
                                    <div class="pull-left">
                                        <h4><i class="fa fa-user fa-fw"></i> User Profile</h4>
                                    </div>
                                    <div class="pull-right">
                                        <div class="btn-group">
                                            <button type="button" class="btn btn-default" id="cancel">
                                                <span class="glyphicon glyphicon-remove-sign"></span> Cancel
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="panel-body">
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <div class="form-group">
                                                <label for="firstName">First Name:</label>
                                                <form:input path="firstName" id="firstName" class="form-control" type="text" value="${person.firstName}" required="required" autofocus="autofocus" />
                                            </div>
                                            <div class="form-group">
                                                <label for="lastName">Last Name:</label>
                                                <form:input path="lastName" id="lastName" class="form-control" type="text" value="${person.lastName}" required="required" />
                                            </div>
                                            <div class="form-group">
                                                <label for="email">Email:</label>
                                                <form:input path="email" id="email" class="form-control" type="text" value="${person.email}" required="required" />
                                            </div>
                                            <div class="form-group">
                                                <label for="workspaceDirectory">Workspace Directory:</label>
                                                <form:input path="workspaceDirectory" id="workspaceDirectory" class="form-control" type="text" value="${person.workspaceDirectory}" required="required" />
                                            </div>
                                            <div class="form-group">
                                                <input type="submit" class="btn btn-lg btn-block btn-success" value="Save">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form:form>
                    </div>
                </div>
                <%@include file="includes/footer.jspf"%>
            </div>
        </div>
        <script src="${path}/vendor/jquery/jquery-2.1.3.min.js"></script>
        <script src="${path}/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="${path}/vendor/metismenu/metisMenu.min.js"></script>
        <script src="${path}/vendor/admin/js/sb-admin-2.js"></script>
        <script>
            $(document).ready(function () {
                $("#view_info").show();
                $("#edit_info").hide();

                $("#edit").click(function () {
                    $("#view_info").hide();
                    $("#edit_info").show();
                });
                $("#cancel").click(function () {
                    $("#view_info").show();
                    $("#edit_info").hide();
                });
            });
        </script>
    </body>
</html>
