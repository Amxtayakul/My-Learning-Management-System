import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Mainframe extends JFrame {

    //CHANGE TO ACCORDING TO DB_NAME, USERNAME, PASSWORD IN SQL
        final String DB_URL = "jdbc:mysql://localhost:3306/*ENTER DBNAME*";
        final String DB_USER = "*ENTER USERNAME*";
        final String DB_PASS = "*ENTER PASSWORD*";

    //DASHBOARD DEPENDING ON USER CLASSIFICATION
    public void initialize(User user) {
        if (user.role.equals("student")) {
            showStudentDashboard(user);
        } else if (user.role.equals("instructor")) {
            showInstructorDashboard(user);
        }

        //LOGOUT BUTTON
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Poppins", Font.BOLD, 14));
        btnLogout.setForeground(Color.RED);
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //CONFIRMATION FOR LOGOUT
                int confirm = JOptionPane.showConfirmDialog(
                    Mainframe.this,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    LoginForm loginForm = new LoginForm();
                    loginForm.initialize();
                }
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        //INITIALIZING THE DASHBOARD
        setTitle("Dashboard");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //STUDENT DASHBOARD
    private void showStudentDashboard(User user) {
        StudentDashboard studentDashboard = new StudentDashboard(user);
        add(studentDashboard, BorderLayout.CENTER);
    }

    //INSTRUCTOR DASHBOARD
    private void showInstructorDashboard(User user) {
        InstructorDashboard instructorDashboard = new InstructorDashboard(user);
        add(instructorDashboard, BorderLayout.CENTER);
    }
}