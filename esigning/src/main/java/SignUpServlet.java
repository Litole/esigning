import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SignUpServlet") 
public class SignUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Database credentials
    private final String jdbcURL = "jdbc:mysql://localhost:3308/e_signature"; // Ensure the DB name and port are correct
    private final String jdbcUsername = "root";
    private final String jdbcPassword = ""; // Check if there is a password for the root user


    // Database insert SQL query
    private static final String INSERT_USERS_SQL = "INSERT INTO users (name, email, phone, password) VALUES (?, ?, ?, ?);";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve form data
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        // Validate password match
        if (!password.equals(confirmPassword)) {
            out.println("<h3>Passwords do not match. Please try again.</h3>");
            return;
        }

        try {
            // Insert data into the database
            insertUser(name, email, phone, password);
            out.println("<h3>Sign Up Successful!</h3>");
            out.println("<a href='login.jsp'>Login</a>");
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<h3>Error occurred while signing up. Please try again later.</h3>");
        }
    }

    // Method to insert user data into the database
    private void insertUser(String name, String email, String phone, String password) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection to the database
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
             
            if (connection != null) {
                System.out.println("Database connection successful!");
            } else {
                System.out.println("Failed to connect to the database.");
            }
            
            // Create a PreparedStatement to insert user data
            preparedStatement = connection.prepareStatement(INSERT_USERS_SQL);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, phone);
            preparedStatement.setString(4, password); // Consider hashing the password for security

            // Execute the query
            preparedStatement.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
