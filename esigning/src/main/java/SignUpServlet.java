import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SignUpServlet") 
public class SignUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Database credentials
    private final String jdbcURL = "jdbc:mysql://localhost:3306/e_signature"; // Check DB name and port
    private final String jdbcUsername = "root";
    private final String jdbcPassword = ""; // Add your MySQL root password if needed

    // SQL query to insert user data
    private static final String INSERT_USERS_SQL = "INSERT INTO users (customer_id, name, email, phone, password) VALUES (?,?,?,?,?);";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve form data
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Get the PrintWriter to output response
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        // Debug logs
        out.println("Received Form Data: Name=" + name + ", Email=" + email);

        // Validate that the password and confirm password match
        if (!password.equals(confirmPassword)) {
            out.println("<h3 style='color:red;'>Passwords do not match. Please try again.</h3>");
            return;
        }

        // Check if fields are not empty
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            out.println("<h3 style='color:red;'>All fields are required. Please fill them in.</h3>");
            return;
        }

        try {
            // Insert data into the database
            out.println("Before executing insertUser function.");
            boolean isInserted = insertUser(name, email, phone, password, out);  // Pass out as parameter
            
            if (isInserted) {
                out.println("<h3 style='color:green;'>Sign Up Successful!</h3>");
                out.println("<a href='login.jsp'>Login</a>");
            } else {
                out.println("<h3 style='color:red;'>Failed to Sign Up. Please try again later.</h3>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<h3 style='color:red;'>An error occurred while signing up. Please try again later.</h3>");
        }
    }

    // Method to insert user data into the database
    public boolean insertUser(String name, String email, String phone, String password, PrintWriter out) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isInserted = false;

        out.println("Starting the try-catch block");

        try {
            // Load MySQL JDBC driver
        	out.println("MySQL JDBC Driver starting successfully.");
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            out.println("MySQL JDBC Driver loaded successfully.");

            // Establish a connection to the database
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);

            if (connection != null) {
                out.println("Database connection successful!");

                // Prepare SQL insert statement
                preparedStatement = connection.prepareStatement(INSERT_USERS_SQL);
                String customer_id = "1"; // Static customer_id for testing (use auto-increment in real case)
                preparedStatement.setString(1, customer_id);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, email);
                preparedStatement.setString(4, phone);
                preparedStatement.setString(5, password); // Consider hashing password for security
                
                // Debugging the prepared statement
                out.println("Prepared statement: " + preparedStatement.toString());
                
                // Execute the query
                int rowsInserted = preparedStatement.executeUpdate();
                
                // Check if rows were inserted
                if (rowsInserted > 0) {
                    out.println("Insert successful. Rows inserted: " + rowsInserted);
                    isInserted = true;
                } else {
                    out.println("Insert failed. No rows were inserted.");
                }
            } else {
                out.println("Failed to connect to the database.");
            }
        } catch (ClassNotFoundException e) {
            out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            out.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }

        return isInserted;
    }
}
