import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import javax.swing.*;

public class LoginForm extends JFrame {
    final private Font mainFont = new Font("Poppins", Font.BOLD, 18);
    JTextField tfUsername; 
    JPasswordField pfPassword; 

    public void initialize(){

        //FORM PANEL
        JLabel lbLoginForm = new JLabel("Login", SwingConstants.CENTER);
        lbLoginForm.setFont(mainFont);

        JLabel lbUsername = new JLabel("Username");
        lbUsername.setFont(mainFont);

        tfUsername = new JTextField();
        tfUsername.setFont(mainFont);
        tfUsername.setPreferredSize(new Dimension(400, 40));
        tfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbPassword = new JLabel("Password");
        lbPassword.setFont(mainFont);

        pfPassword = new JPasswordField();
        pfPassword.setFont(mainFont);
        pfPassword.setPreferredSize(new Dimension(400, 40));
        pfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        formPanel.add(lbLoginForm);
        formPanel.add(lbUsername);
        formPanel.add(tfUsername);
        formPanel.add(lbPassword);
        formPanel.add(pfPassword);

        //BUTTONS
        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(mainFont);
        btnLogin.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = tfUsername.getText();
                String pass = String.valueOf(pfPassword.getPassword());

                User user = getAuthenticatedUser(username, pass);

                if(user != null) {
                    Mainframe mainFrame = new Mainframe();
                    mainFrame.initialize(user);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginForm.this,
                        "User ID or Password Invalid",
                        "Try Again",
                        JOptionPane.ERROR_MESSAGE
                    );
                }


            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(mainFont);
        btnCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        buttonsPanel.add(btnLogin);
        buttonsPanel.add(btnCancel);

        //INTIALIZING THE FRAME
        add(formPanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.SOUTH);

        setTitle("Login");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(500,400);
        setMinimumSize(new Dimension(450, 350));
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //  AUTHENTICATION
    private User getAuthenticatedUser(String username, String pass) {
        User user = null;

        //CHANGE TO ACCORDING TO DB_NAME, USERNAME, PASSWORD IN SQL
        final String DB_URL = "jdbc:mysql://localhost:3306/*ENTER DBNAME*";
        final String DB_USER = "*ENTER USERNAME*";
        final String DB_PASS = "*ENTER PASSWORD*";

        //ESTABLISH CONNECTION
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "SELECT * FROM LOGIN WHERE username=? AND pass=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, pass);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = new User();
                user.username = resultSet.getString("username");
                user.pass = resultSet.getString("pass");
                user.role = resultSet.getString("role");
                user.student_ID = resultSet.getString("student_ID");
                user.instructor_ID = resultSet.getString("instructor_ID");
            }
            preparedStatement.close();
            conn.close();
        }catch(Exception e){
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
        return user;
    }
    public static void main(String[] args) {
        LoginForm loginForm = new LoginForm();
        loginForm.initialize();
    }
}