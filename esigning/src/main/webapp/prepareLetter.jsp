<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Prepare Letter of Offer</title>
</head>
<body>
    <h2>Prepare Letter of Offer</h2>
    <form action="PrepareLetterServlet" method="post" enctype="multipart/form-data">
        <label for="customerId">Customer ID:</label>
        <input type="text" id="customerId" name="customerId" required>
        <br><br>
        
        <label for="document">Upload Document:</label>
        <input type="file" id="document" name="document" accept=".pdf,.docx,.doc" required>
        <br><br>
        
        <input type="submit" value="Prepare and Send Letter">
    </form>
</body>
</html>
