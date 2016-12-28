<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
  <head>  
    <title>AngularJS $http Example</title>  
<c:set var="req" value="${pageContext.request}" />
    <c:set var="http_base" value="${req.scheme}://${req.serverName}:${req.serverPort}${req.contextPath}/"/>
    <base href="${pageContext.request.secure ? fn:replace(http_base, ':443/', '/') : fn:replace(http_base, ':80/', '/')}"/>
    
     <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
     <link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
  </head>
<body class="ng-cloak">    
<h1>Login draft page...</h1>
</body>
</html>