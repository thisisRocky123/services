import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserServlet extends HttpServlet {
    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/home_services";
        String user = "root"; // replace with your DB username
        String password = "password"; // replace with your DB password
        return DriverManager.getConnection(url, user, password);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("register".equals(action)) {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");
            String password = hashPassword(request.getParameter("password"));
            
            try (Connection conn = connect()) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Users (name, email, phone, address, password) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, phone);
                stmt.setString(4, address);
                stmt.setString(5, password);
                stmt.executeUpdate();
                response.sendRedirect("login.jsp");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if ("login".equals(action)) {
            String email = request.getParameter("email");
            String password = hashPassword(request.getParameter("password"));
            
            try (Connection conn = connect()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE email = ? AND password = ?");
                stmt.setString(1, email);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    HttpSession session = request.getSession();
                    session.setAttribute("user", rs.getString("name"));
                    response.sendRedirect("dashboard.jsp");
                } else {
                    response.sendRedirect("login.jsp?error=Invalid credentials");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}