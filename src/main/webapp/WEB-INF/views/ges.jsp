<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
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
        <title>CCD: GES</title>
        <meta name="description" content="" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${path}/vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="${path}/vendor/metismenu/metisMenu.min.css" />
        <link rel="stylesheet" href="${path}/vendor/font_awesome/css/font-awesome.min.css" />
        <link rel="stylesheet" href="${path}/vendor/admin/css/sb-admin-2.css" />
        <link rel="stylesheet" href="${path}/css/styles.css" />
        <link rel="stylesheet" href="${path}/css/stepwizard.css" />
        <script src="${path}/vendor/angularjs/angular.min.js"></script>
    </head>
    <body ng-app>
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
                        <h1 class="page-header">GES</h1>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <div class="stepwizard">
                            <div class="stepwizard-row setup-panel">
                                <div class="stepwizard-step">
                                    <a href="#step-1" type="button" class="btn btn-primary btn-circle">1</a>
                                    <p>Chose Dataset</p>
                                </div>
                                <div class="stepwizard-step">
                                    <a href="#step-2" type="button" class="btn btn-default btn-circle" disabled="disabled">2</a>
                                    <p>Set Parameters</p>
                                </div>
                                <div class="stepwizard-step">
                                    <a href="#step-3" type="button" class="btn btn-default btn-circle" disabled="disabled">3</a>
                                    <p>Run Algorithm</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <form:form method="POST" action="" modelAttribute="gesRunInfo" role="form" >
                            <div class="row setup-content" id="step-1">
                                <div class="col-xs-12">
                                    <div class="col-md-12">
                                        <h3> Step 1: Chose Your Dataset</h3>
                                        <div class="form-group">
                                            <form:select path="dataset" id="dataset" items="${dataset}" class="form-control" required="required" ng-model="dataset" />
                                        </div>
                                        <div class="form-group">
                                            <div class="checkbox">
                                                <label for="continuous" class="control-label">
                                                    <form:checkbox path="continuous" id="continuous" ng-model="continuous" ng-init="continuous=${gesRunInfo.continuous}" />
                                                    Continuous Variables
                                                </label>
                                            </div>
                                        </div>
                                        <button type="button" class="btn btn-primary btn-lg center-block nextBtn">Next</button>
                                    </div>
                                </div>
                            </div>
                            <div class="row setup-content" id="step-2">
                                <div class="col-xs-12">
                                    <div class="col-md-12">
                                        <h3> Step 2: Algorithm Parameters</h3>
                                        <div class="form-group">
                                            <label for="penaltyDiscount" class="control-label">Penalty Discount</label>
                                            <form:input path="penaltyDiscount" id="penaltyDiscount" maxlength="10" type="text" required="required" class="form-control" placeholder="Penalty Discount" ng-model="penaltyDiscount" ng-init="penaltyDiscount=${gesRunInfo.penaltyDiscount}" />
                                        </div>
                                        <div class="form-group">
                                            <div class="checkbox">
                                                <label for="excludeZeroCorrelationEdges" class="control-label">
                                                    <form:checkbox path="excludeZeroCorrelationEdges" id="excludeZeroCorrelationEdges" ng-model="excludeZeroCorrelationEdges" ng-init="excludeZeroCorrelationEdges=${gesRunInfo.excludeZeroCorrelationEdges}" />
                                                    Exclude Zero Correlation Edges
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <div class="checkbox">
                                                <label for="verbose" class="control-label">
                                                    <form:checkbox path="verbose" id="verbose" ng-model="verbose" ng-init="verbose=${gesRunInfo.verbose}" />
                                                    Verbose
                                                </label>
                                            </div>
                                        </div>
                                        <button type="button" class="btn btn-primary btn-lg center-block nextBtn">Next</button>
                                    </div>
                                </div>
                            </div>
                            <div class="row setup-content" id="step-3">
                                <div class="col-xs-12">
                                    <div class="col-md-12">
                                        <h3> Step 3: Run the Algorithm</h3>
                                        <div class="panel panel-default">
                                            <div class="panel-heading">Summary</div>
                                            <div class="panel-body">
                                                <div class="table-responsive">
                                                    <table class="table table-striped">
                                                        <tbody>
                                                            <tr>
                                                                <td class="col-md-3">Dataset:</td>
                                                                <td ng-bind="dataset"></td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-md-3">Continuous Variables</td>
                                                                <td ng-bind="continuous"></td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-md-3">Penalty Discount:</td>
                                                                <td ng-bind="penaltyDiscount"></td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-md-3">Exclude Zero Correlation Edges</td>
                                                                <td ng-bind="excludeZeroCorrelationEdges"></td>
                                                            </tr>
                                                            <tr>
                                                                <td class="col-md-3">Verbose:</td>
                                                                <td ng-bind="verbose"></td>
                                                            </tr>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                        <button type="submit" class="btn btn-success btn-lg center-block nextBtn">Run Algorithm!</button>
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
        <script src="${path}/js/stepwizard.js"></script>
        <script src="${path}/js/algorithm.js"></script>
    </body>
</html>
