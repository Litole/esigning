import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database credentials
    private final String jdbcURL = "jdbc:mysql://localhost:3306/e_signature"; // Change database URL if needed
    private final String jdbcUsername = "root"; // Update with your MySQL username
    private final String jdbcPassword = ""; // Update with your MySQL password

    // SQL query to fetch the user by email (or username)
    private static final String SELECT_USER_SQL = "SELECT * FROM users WHERE email = ?";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve form data
        String email = request.getParameter("username"); // Assuming email is used as the username
        String password = request.getParameter("password");

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        try {
            // Validate the user
            if (validateUser(email, password)) {
                // If the user is validated, redirect to a welcome page or dashboard
                response.sendRedirect("welcome.jsp");
            } else {
                // If login fails, redirect back to the login page with an error message
                response.sendRedirect("login.jsp?error=Invalid username or password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<h3>Error occurred while logging in. Please try again later.</h3>");
        }
    }

    // Method to validate the user by comparing the email and password in the database
    private boolean validateUser(String email, String password) throws SQLException {
        boolean isValidUser = false;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection to the database
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);

            // Create a PreparedStatement to find the user by email
            preparedStatement = connection.prepareStatement(SELECT_USER_SQL);
            preparedStatement.setString(1, email);

            // Execute the query
            resultSet = preparedStatement.executeQuery();

            // Check if a user exists with the given email
            if (resultSet.next()) {
                // Retrieve the password stored in the database
                String storedPassword = resultSet.getString("password");

                // Compare the provided password with the stored password
                if (storedPassword.equals(password)) {
                    isValidUser = true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }

        return isValidUser;
    }
}
