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
        <title>CCD: Home</title>
        <meta name="description" content="" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="vendor/metismenu/metisMenu.min.css" />
        <link rel="stylesheet" href="vendor/font_awesome/css/font-awesome.min.css" />
        <link rel="stylesheet" href="css/styles.css" />
        <link rel="stylesheet" href="vendor/admin/css/sb-admin-2.css" />
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
                        <h2 class="page-header">Welcome ${appUser.person.firstName} ${appUser.person.lastName}</h2>
                    </div>
                </div>

                <div class="row">
                    <div class="col-lg-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h4>CCD Application</h4>
                            </div>
                            <div class="panel-body">
                                <p>The application provides the following functions:</p>
                                <ol>
                                    <li>Upload of one or more data files.</li>
                                    <li>Running of a causal discovery algorithm.</li>
                                    <li>Display of causal discovery algorithm results as a graph or downloadable file.</li>
                                </ol>
                                <div class="col-sm-11">
                                    <h4 class="page-header">Upload of Data</h4>
                                    <p>
                                        Users can upload multiple files to the server using a file browser or by drag-and-drop.
                                        The status bar indicates the progress of file transfer.
                                        The system supports resumable file uploads and detects file corruption using MD5 hashes.
                                        Users can view a list of uploaded data and optionally delete files on the server.
                                    </p>
                                </div>
                                <div class="col-sm-11">
                                    <h4 class="page-header">Run Algorithm</h4>
                                    <p>A run algorithm wizard guides the user through the following process:</p>
                                    <ol>
                                        <li>Selecting a data file for analysis.</li>
                                        <li>Setting the parameters for the selected algorithm.</li>
                                        <li>Reviewing the parameters prior to running the algorithm.</li>
                                    </ol>
                                    <p>
                                        The users may go back to any step to change the selected data file or parameters, however, users may not skip any steps.
                                    </p>
                                </div>
                                <div class="col-sm-11">
                                    <h4 class="page-header">Show Results</h4>
                                    <p>
                                        Users can view the results of an analysis as a graph or downloaded file.
                                        The application has a built in graphing tool to help visualize the output results.
                                        Users can view the graph by clicking on the result's filename.
                                    </p>
                                    <p>
                                        The results can be downloaded to the users' local computer for further analysis.
                                        The users can delete any results off the server that are no longer needed.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <%@include file="includes/footer.jspf" %>

            </div>
        </div>
        <script src="vendor/jquery/jquery-2.1.3.min.js"></script>
        <script src="vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="vendor/metismenu/metisMenu.min.js"></script>
        <script src="vendor/admin/js/sb-admin-2.js"></script>
    </body>
</html>
