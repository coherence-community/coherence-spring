<%@page session="true" %>
<html>
<head></head>
<body>
<h1>Hello World!</h1>
<div>
    Counter: <%= request.getSession().getAttribute("counter") %>
</div>
</body>
</html>
