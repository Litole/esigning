<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
</head>
<body>
    <h2>Login</h2>
    <form action="LoginServlet" method="post">
        <label for="username">Username (Email):</label><br>
        <input type="text" id="username" name="username" required><br>
        <label for="password">Password:</label><br>
        <input type="password" id="password" name="password" required><br><br>
        <input type="submit" value="Login">
    </form>
    <p style="color:red;">
        <%= request.getParameter("error") != null ? request.getParameter("error") : "" %>
    </p>
    <a href="signup.jsp">Sign up</a>
</body>
</html>
