<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>마인크래프트 대학</title>
</head>
<body>
    <form action="/user/login" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label for="userId"></label><input id="userId" name="userId" type="text">
        <label for="password"></label><input id="password" name="password" type="password">
        <input type="submit">
    </form>
</body>
</html>
