import java.awt.*;
import java.sql.*;
import javax.swing.*;
import java.awt.event.*;

public class StudentDashboard extends JPanel {

  //CHANGE TO ACCORDING TO DB_NAME, USERNAME, PASSWORD IN SQL
        final String DB_URL = "jdbc:mysql://localhost:3306/*ENTER DBNAME*";
        final String DB_USER = "*ENTER USERNAME*";
        final String DB_PASS = "*ENTER PASSWORD*";

    public StudentDashboard(User user) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildInfoPanel(user), BorderLayout.NORTH);
        add(buildCoursePanel(user), BorderLayout.CENTER);
    }

    private JPanel buildInfoPanel(User user) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Student Information"));

        //ESTABLISH CONNECTION
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "SELECT s.*, p.Program_Name FROM STUDENT s " +
                         "JOIN PROGRAM p ON s.program_ID = p.Program_ID " +
                         "WHERE s.student_ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.student_ID);
            ResultSet rs = ps.executeQuery();

            //DISPLAY STUDENT TABLE
            if (rs.next()) {
                panel.add(new JLabel("Student ID:"));
                panel.add(new JLabel(rs.getString("student_ID")));

                panel.add(new JLabel("Name:"));
                panel.add(new JLabel(rs.getString("student_name")));

                panel.add(new JLabel("Program:"));
                panel.add(new JLabel(rs.getString("Program_Name")));

                panel.add(new JLabel("Year:"));
                panel.add(new JLabel(rs.getString("student_Year")));

                panel.add(new JLabel("Section:"));
                panel.add(new JLabel(rs.getString("student_Section")));

                panel.add(new JLabel("Contact:"));
                panel.add(new JLabel(rs.getString("student_Contact")));
            }

            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            panel.add(new JLabel("Failed to load student info."));
        }

        return panel;
    }

    //COURSE PANEL
    private JPanel buildCoursePanel(User user) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("My Course"));

        //SEARCH PANEL
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        JTextField tfSearch = new JTextField();
        tfSearch.setFont(new Font("Poppins", Font.PLAIN, 13));
        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Poppins", Font.PLAIN, 13));
        searchPanel.add(new JLabel("Search Lesson: "), BorderLayout.WEST);
        searchPanel.add(tfSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        //LESSON PANEL
        JPanel lessonsPanel = new JPanel();
        lessonsPanel.setLayout(new BoxLayout(lessonsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(lessonsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        loadLessons(user, lessonsPanel, "");

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLessons(user, lessonsPanel, tfSearch.getText().trim());
            }
        });

        tfSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLessons(user, lessonsPanel, tfSearch.getText().trim());
            }
        });

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    //LOAD LESSONS
    private void loadLessons(User user, JPanel lessonsPanel, String keyword) {
        lessonsPanel.removeAll();

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // CHECK IF ENROLLED
            String checkSql = "SELECT * FROM ENROLLMENT WHERE student_ID = ? AND enrollment_Status = 'Enrolled'";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, user.student_ID);
            ResultSet checkRs = checkPs.executeQuery();

            //DISPLAY FOR NOT ENROLLED STUDENTS
            if (!checkRs.next()) {
                JLabel notEnrolled = new JLabel("You are not yet enrolled.");
                notEnrolled.setFont(new Font("Poppins", Font.BOLD, 16));
                notEnrolled.setForeground(Color.RED);
                notEnrolled.setAlignmentX(Component.CENTER_ALIGNMENT);
                lessonsPanel.add(notEnrolled);
            
            //FOR ENROLLED STUDENTS
            } else {
                String sql = "SELECT c.course_ID, c.course_name, c.course_Units, " +
                            "l.lesson_ID, l.lesson_title, l.lesson_Content " +
                            "FROM ENROLLMENT e " +
                            "JOIN COURSE c ON e.course_ID = c.course_ID " +
                            "LEFT JOIN LESSON l ON l.course_ID = c.course_ID " +
                            "WHERE e.student_ID = ? AND e.enrollment_Status = 'Enrolled' " +
                            "AND (? = '' OR l.lesson_title LIKE ?) " +
                            "ORDER BY c.course_ID, l.lesson_ID";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user.student_ID);
                ps.setString(2, keyword);
                ps.setString(3, "%" + keyword + "%");
                ResultSet rs = ps.executeQuery();

                String lastCourse = "";
                boolean anyResults = false;
                
                while (rs.next()) {
                    anyResults = true;
                    String courseID      = rs.getString("course_ID");
                    String courseName    = rs.getString("course_name");
                    int    units         = rs.getInt("course_Units");
                    String lessonID      = rs.getString("lesson_ID");
                    String lessonTitle   = rs.getString("lesson_title");
                    String lessonContent = rs.getString("lesson_Content");

                    if (!courseID.equals(lastCourse)) {
                        JLabel courseLabel = new JLabel(courseID + " - " + courseName + " (" + units + " units)");
                        courseLabel.setFont(new Font("Poppins", Font.BOLD, 14));
                        courseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        courseLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
                        lessonsPanel.add(courseLabel);
                        lastCourse = courseID;
                    }
 
                    if (lessonID != null) {
                        JLabel lessonLabel = new JLabel("    • " + lessonTitle);
                        lessonLabel.setFont(new Font("Poppins", Font.BOLD, 13));
                        lessonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        lessonsPanel.add(lessonLabel);

                        if (lessonContent != null && !lessonContent.isEmpty()) {
                            JTextArea contentArea = new JTextArea(lessonContent);
                            contentArea.setFont(new Font("Poppins", Font.PLAIN, 12));
                            contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
                            contentArea.setLineWrap(true);
                            contentArea.setWrapStyleWord(true);
                            contentArea.setEditable(false);
                            contentArea.setBackground(lessonsPanel.getBackground());
                            contentArea.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 0));
                            lessonsPanel.add(contentArea);
                        } else {
                            JLabel noContent = new JLabel("      No content available.");
                            noContent.setFont(new Font("Poppins", Font.ITALIC, 12));
                            noContent.setAlignmentX(Component.LEFT_ALIGNMENT);
                            noContent.setForeground(Color.GRAY);
                            lessonsPanel.add(noContent);
                        }
                    }
                }

                if (!anyResults && !keyword.isEmpty()) {
                    JLabel noResult = new JLabel("No lessons found for \"" + keyword + "\".");
                    noResult.setFont(new Font("Poppins", Font.ITALIC, 13));
                    noResult.setAlignmentX(Component.LEFT_ALIGNMENT);
                    noResult.setForeground(Color.GRAY);
                    noResult.setAlignmentX(Component.CENTER_ALIGNMENT);
                    lessonsPanel.add(noResult);
                }

                ps.close();
            }

            checkPs.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            lessonsPanel.add(new JLabel("Failed to load lessons."));
        }

        lessonsPanel.revalidate();
        lessonsPanel.repaint();
    }
}
