<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="vendor/metismenu/metisMenu.min.css" />
        <link rel="stylesheet" href="vendor/font_awesome/css/font-awesome.min.css" />
        <link rel="stylesheet" href="vendor/admin/css/sb-admin-2.css" />
        <link rel="stylesheet" href="css/styles.css" />
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
	               	&nbsp;
				</div>

				<c:if test="${empty person}">
	                <div class="row">
	                    <div class="col-lg-12">
	                        <div class="panel panel-default">
	                            <div class="panel-heading clearfix">
	                                <h4 class="pull-left"><i class="fa fa-user fa-fw"></i> User Profile</h4>
	
                                    <form:form method="POST" action="${path}/userprofile" 
                                   		modelAttribute="person" role="form" >
		                                <button type=submit class="btn btn-default pull-right">
		                                	<span class="glyphicon glyphicon-edit"></span> Edit
		                                </button>
	                                </form:form>
	                            </div>
	                            <div class="panel-body">
	                                <div class="col-sm-12">
                 						<div class="row">
			                                <c:if test="${not empty successMsg}">
			                                    <div class="alert alert-success text-center" role="alert">${successMsg}</div>
			                                	<c:remove var="successMsg" scope="session" />
			                                </c:if>
			                            </div>
	                                	<div class="row">
	                                		<div class="col-md-3">
	                                			<b>First Name:</b>
	                                		</div>
	                                		<div class="col-md-9">
	                                			${appUser.person.firstName}
	                                		</div>
	                                	</div>
	                                	<div class="row">
	                                		<div class="col-md-3">
	                                			<b>Last Name:</b>
	                                		</div>
	                                		<div class="col-md-9">
	                                			${appUser.person.lastName}
	                                		</div>
	                                	</div>
	                                	<div class="row">
	                                		<div class="col-md-3">
	                                			<b>E-mail:</b>
	                                		</div>
	                                		<div class="col-md-9">
	                                			${appUser.person.email}
	                                		</div>
	                                	</div>
	                                	<div class="row">
	                                		<div class="col-md-3">
	                                			<b>Workspace:</b>
	                                		</div>
	                                		<div class="col-md-9">
	                                			${appUser.person.workspaceDirectory}
	                                		</div>
	                                	</div>
	                                	<div class="row">
	                                		<div class="col-md-3">
	                                			<b>Account Created:</b>
	                                		</div>
	                                		<div class="col-md-9">
	                                			${appUser.createdDate}
	                                		</div>
	                                	</div>
	                                	<div class="row">
	                                		<div class="col-md-3">
	                                			<b>Last Login:</b>
	                                		</div>
	                                		<div class="col-md-9">
	                                			${appUser.lastLoginDate}
	                                		</div>
	                                	</div>
	                                </div>
	                            </div>
	                        </div>
	                    </div>
	                </div>
				</c:if>

				<c:if test="${not empty person}">
	                <div class="row">
	                    <div class="col-lg-12">
                           <form:form id="userSetup" method="POST" action="${path}/userprofile" 
	                           	modelAttribute="person" role="form" >
		                        <div class="panel panel-default">
		                            <div class="panel-heading clearfix">
		                                <h4 class="pull-left"><i class="glyphicon glyphicon-edit"></i> Edit User Profile</h4>
		                                <div class="btn-group pull-right" role="group">
			                                <button type="submit" class="btn btn-default">
			                                	<span class="glyphicon glyphicon-floppy-disk"></span> Save
			                                </button>
			                                <a href="${path}/userprofile" class="btn btn-default" role="button">
			                                	<span class="glyphicon glyphicon-remove-sign"></span> Cancel
			                                </a>
		                                </div>
		                            </div>
		                            <div class="panel-body">
			                            <div class="row">
			                                <c:if test="${not empty errorMsg}">
			                                    <div class="alert alert-danger text-center" role="alert">${errorMsg}</div>
			                                </c:if>
			                            </div>
			                            <div class="row">
			                                <div class="col-lg-11">
		                                        <div class="form-group">
		                                            <label for="firstName">First Name:</label>
		                                            <form:input path="firstName" id="firstName" class="form-control" 
		                                            	type="text" value="${person.firstName}" required="required" 
		                                            	autofocus="autofocus" title="" ></form:input>
		                                        </div>
		                                        <div class="form-group">
		                                            <label for="lastName">Last Name:</label>
		                                            <form:input path="lastName" id="lastName" class="form-control" 
		                                            	type="text" value="${person.lastName}" required="required" 
		                                            	title="Last name is a name that your teacher calls you i.e. (Mr.) Doe." ></form:input>
		                                        </div>
		                                        <div class="form-group">
		                                            <label for="email">Email:</label>
		                                            <form:input path="email" id="email" class="form-control" type="text" 
		                                            	value="${person.email}" required="required"
		                                            	title="Email (address) is your electronic post office box i.e. john@doe.net." ></form:input>
		                                        </div>
		                                        <div class="form-group">
		                                            <label for="workspaceDirectory">Workspace Directory:</label>
		                                            <form:input path="workspaceDirectory" id="workspaceDirectory" class="form-control" 
		                                            	type="text" value="${person.workspaceDirectory}" required="required" 
		                                            	title="Workspace directory is a folder that you want to instill this application on." ></form:input>
		                                        </div>
											</div>
										</div>	
		                            </div>
		                        </div>
							</form:form>
	                    </div>
	                </div>
				</c:if>
                
                <%@include file="includes/footer.jspf" %>
            </div>
        </div> 
        
        <script src="vendor/jquery/jquery-2.1.3.min.js"></script>
        <script src="vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="vendor/jquery/jquery.validate.min.js"></script>
        <script src="vendor/metismenu/metisMenu.min.js"></script>
        <script src="vendor/admin/js/sb-admin-2.js"></script>
        <script src="js/setup.js"></script>
    </body>
</html>
