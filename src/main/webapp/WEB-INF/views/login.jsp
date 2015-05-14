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
        <title>CCD: Login</title>
        <meta name="description" content="" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap-theme.min.css" />
        <link rel="stylesheet" href="css/styles.css" />
        <link rel="stylesheet" href="css/login.css" />
        <script src="vendor/modernizr/modernizr-2.8.3-respond-1.4.2.min.js"></script>
    </head>
    <body>
        <div class="container">
            <div class="row">
                <div class="col-md-4 col-md-offset-4">
                    <div class="login-panel panel panel-pitt">
                        <div class="panel-heading">
                            <h3 class="panel-title">${ccd.title}</h3>
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <div class="center-block">
                                    <img class="profile-img" src="img/login-user.png" alt="profile picture" />
                                </div>
                            </div>
                            <div class="row">
                                <c:if test="${not empty successMsg}">
                                    <div class="alert alert-success text-center" role="alert">${successMsg}</div>
                                </c:if>
                                <c:if test="${not empty errorMsg}">
                                    <div class="alert alert-danger text-center" role="alert">${errorMsg}</div>
                                </c:if>
                            </div>
                            <div class="row">
                                <div class="col-sm-12 col-md-10  col-md-offset-1">
                                    <form id="login" role="form" method="POST" action="${path}/login">
                                        <div class="form-group">
                                            <div class="input-group">
                                                <span class="input-group-addon">
                                                    <i class="glyphicon glyphicon-user pitt-color"></i>
                                                </span> 
                                                <input class="form-control" placeholder="Username" name="username" type="text" required="required" autofocus>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <div class="input-group">
                                                <span class="input-group-addon">
                                                    <i class="glyphicon glyphicon-lock pitt-color"></i>
                                                </span>
                                                <input class="form-control" placeholder="Password" name="password" type="password" required="required" value="">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <input type="checkbox" name="rememberMe" value="true" checked="" /> Remember Me
                                        </div>
                                        <div class="form-group">
                                            <input type="submit" class="btn btn-lg btn-block btn-pitt" value="Sign In">
                                        </div>
                                    </form>
                                    <div style="border-top: 1px solid #ddd; padding-top:15px; font-size:85%">
                                        Don't have an account? <a href="#" data-toggle="modal" data-target="#signup">Sign Up Here!</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="text-center">
                    <p class="text-muted">Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> ${ccd.copyright}</p>
                </div>
            </div>
        </div>
        <div class="modal fade" id="signup" tabindex="-1" role="dialog" aria-labelledby="signup" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>
                        <h4 class="modal-title pitt-color">CCD Registration</h4>
                    </div>
                    <div class="modal-body">
                        <form id="registration" role="form" method="POST" action="${path}/registration">
                            <fieldset>
                                <div class="form-group">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="glyphicon glyphicon-user pitt-color"></i>
                                        </span> 
                                        <input value="kvb2" class="form-control" placeholder="Username" name="username" type="text" required="required" autofocus>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="glyphicon glyphicon-envelope pitt-color"></i>
                                        </span> 
                                        <input value="kvb2@pitt.edu" class="form-control" placeholder="Email" name="email" type="text" required="required" autofocus>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="glyphicon glyphicon-lock pitt-color"></i>
                                        </span> 
                                        <input value="hello" id="password" class="form-control" placeholder="Password" name="password" type="password" required="required" autofocus>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="glyphicon glyphicon-lock pitt-color"></i>
                                        </span> 
                                        <input value="hello" class="form-control" placeholder="Confirm Password" name="confirmPassword" type="password" required="required" autofocus>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <input type="checkbox" name="agree" value="true" checked="" /> Agree to <a href="#agreement" data-toggle="modal" data-target="#agreement">Terms &amp; Conditions</a>
                                </div>
                                <button id="register" type="submit" class="btn btn-success pull-right">Sign Up</button>
                            </fieldset>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal fade" id="agreement" tabindex="-1" role="dialog" aria-labelledby="agreement" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title pitt-color">Terms &amp; Conditions</h4>
                    </div>
                    <div class="modal-body">
                        <p>You agree to Blah blah blah blah...</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
        <script src="vendor/jquery/jquery-2.1.3.min.js"></script>
        <script src="vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="vendor/jquery/jquery.validate.min.js"></script>
        <script>
            $(document).ready(function () {
                $('#login').validate({
                    rules: {
                        username: {
                            minlength: 4,
                            required: true
                        },
                        password: {
                            minlength: 5,
                            maxlength: 25,
                            required: true
                        }
                    },
                    messages: {
                        username: "Please enter your username.",
                        password: "Please enter your password."
                    },
                    highlight: function (element) {
                        $(element).closest('.form-group').addClass('has-error');
                    },
                    unhighlight: function (element) {
                        $(element).closest('.form-group').removeClass('has-error');
                    },
                    errorElement: 'span',
                    errorClass: 'help-block',
                    errorPlacement: function (error, element) {
                        if (element.parent('.input-group').length) {
                            error.insertAfter(element.parent());
                        } else {
                            error.insertAfter(element);
                        }
                    }
                });
                $('#registration').validate({
                    rules: {
                        username: {
                            minlength: 4,
                            required: true
                        },
                        email: {
                            email: true,
                            required: true
                        },
                        password: {
                            minlength: 5,
                            maxlength: 25,
                            required: true
                        },
                        confirmPassword: {
                            equalTo: "#password"
                        }
                    },
                    messages: {
                        username: "Please enter a username.",
                        email: "Please enter a valid email.",
                        password: "Please enter a password."
                    },
                    highlight: function (element) {
                        $(element).closest('.form-group').addClass('has-error');
                    },
                    unhighlight: function (element) {
                        $(element).closest('.form-group').removeClass('has-error');
                    },
                    errorElement: 'span',
                    errorClass: 'help-block',
                    errorPlacement: function (error, element) {
                        if (element.parent('.input-group').length) {
                            error.insertAfter(element.parent());
                        } else {
                            error.insertAfter(element);
                        }
                    }
                });
            });
        </script>
    </body>
</html>
