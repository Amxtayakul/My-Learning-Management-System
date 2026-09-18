import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class InstructorDashboard extends JPanel {

    //CHANGE TO ACCORDING TO DB_NAME, USERNAME, PASSWORD IN SQL
        final String DB_URL = "jdbc:mysql://localhost:3306/*ENTER DBNAME*";
        final String DB_USER = "*ENTER USERNAME*";
        final String DB_PASS = "*ENTER PASSWORD*";

    User user;

    public InstructorDashboard(User user) {
        this.user = user;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildInfoPanel(), BorderLayout.NORTH);
        add(buildCoursePanel(), BorderLayout.CENTER);
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Instructor Information"));

        //ESTABLISH CONNECTION
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "SELECT * FROM INSTRUCTOR WHERE instructor_ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.instructor_ID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                panel.add(new JLabel("Instructor ID:"));
                panel.add(new JLabel(String.valueOf(rs.getInt("instructor_ID"))));

                panel.add(new JLabel("Name:"));
                panel.add(new JLabel(rs.getString("instructor_Name")));

                panel.add(new JLabel("Contact:"));
                panel.add(new JLabel(rs.getString("instructor_Contact")));
            }

            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            panel.add(new JLabel("Failed to load instructor info."));
        }

        return panel;
    }

    //COURSE PANEL
    private JPanel buildCoursePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("My Courses"));

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "SELECT * FROM COURSE WHERE instructor_ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.instructor_ID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String courseID = rs.getString("course_ID");
                String courseName = rs.getString("course_name");
                int units = rs.getInt("course_Units");

                JLabel courseLabel = new JLabel(courseID + " - " + courseName + " (" + units + " units)");
                courseLabel.setFont(new Font("Poppins", Font.BOLD, 14));

                //VIEW STUDENTS BUTTON
                JButton btnViewStudents = new JButton("View Students");
                btnViewStudents.setFont(new Font("Poppins", Font.PLAIN, 12));
                btnViewStudents.setForeground(new Color(0, 100, 200));
                btnViewStudents.putClientProperty("courseID", courseID);
                btnViewStudents.putClientProperty("courseName", courseName);
                btnViewStudents.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String cID = (String) btnViewStudents.getClientProperty("courseID");
                        String cName = (String) btnViewStudents.getClientProperty("courseName");
                        showEnrolledStudents(cID, cName);
                    }
                });

                JPanel courseHeader = new JPanel(new BorderLayout(5, 0));
                courseHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                courseHeader.add(courseLabel, BorderLayout.CENTER);
                courseHeader.add(btnViewStudents, BorderLayout.EAST);
                panel.add(courseHeader);

                //LESSON PANEL
                JPanel lessonsPanel = buildLessonsPanel(courseID);
                panel.add(lessonsPanel);

                //ADD LESSON BUTTON
                JButton btnAddLesson = new JButton("+ Add Lesson to " + courseID);
                btnAddLesson.setFont(new Font("Poppins", Font.PLAIN, 12));
                btnAddLesson.putClientProperty("courseID", courseID);
                btnAddLesson.putClientProperty("lessonsPanel", lessonsPanel);
                btnAddLesson.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String cID = (String) btnAddLesson.getClientProperty("courseID");
                        JPanel lPanel = (JPanel) btnAddLesson.getClientProperty("lessonsPanel");
                        showAddLessonDialog(cID, lPanel);
                    }
                });

                panel.add(btnAddLesson);
                panel.add(Box.createVerticalStrut(10));
            }

            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            panel.add(new JLabel("Failed to load courses."));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    //BUILD LESSON PANEL
    private JPanel buildLessonsPanel(String courseID) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        refreshLessons(panel, courseID);
        return panel;
    }

    //REFRESH LESSON PANEL
    private void refreshLessons(JPanel panel, String courseID) {
        panel.removeAll();

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "SELECT * FROM LESSON WHERE course_ID = ? ORDER BY lesson_ID";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, courseID);
            ResultSet rs = ps.executeQuery();

            boolean hasLessons = false;
            while (rs.next()) {
                hasLessons = true;
                int    lessonID      = rs.getInt("lesson_ID");
                String lessonTitle   = rs.getString("lesson_title");
                String lessonContent = rs.getString("lesson_Content");

                JPanel lessonRow = new JPanel(new BorderLayout(5, 0));
                lessonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                JPanel textPanel = new JPanel();
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

                JLabel titleLabel = new JLabel("  • " + lessonTitle);
                titleLabel.setFont(new Font("Poppins", Font.BOLD, 13));
                textPanel.add(titleLabel);

                if (lessonContent != null && !lessonContent.isEmpty()) {
                    JLabel contentLabel = new JLabel("    " + lessonContent);
                    contentLabel.setFont(new Font("Poppins", Font.PLAIN, 12));
                    contentLabel.setForeground(Color.GRAY);
                    textPanel.add(contentLabel);
                } else {
                    JLabel noContent = new JLabel("    No content yet.");
                    noContent.setFont(new Font("Poppins", Font.ITALIC, 12));
                    noContent.setForeground(Color.GRAY);
                    textPanel.add(noContent);
                }

                //EDIT LESSON BUTTON - POSITIONED LEFT BESIDE THE DELETE
                JButton btnEdit = new JButton("Edit");
                btnEdit.setFont(new Font("Poppins", Font.PLAIN, 11));
                btnEdit.setForeground(new Color(0, 120, 0));
                btnEdit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showEditLessonDialog(lessonID, lessonTitle, lessonContent, panel, courseID);
                    }
                });

                //DELETE LESSON BUTTON
                JButton btnDelete = new JButton("Delete");
                btnDelete.setFont(new Font("Poppins", Font.PLAIN, 11));
                btnDelete.setForeground(Color.RED);
                btnDelete.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Delete lesson: " + lessonTitle + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteLesson(lessonID, panel, courseID);
                        }
                    }
                });

                JPanel actionPanel = new JPanel(new GridLayout(1, 2, 4, 0));
                actionPanel.add(btnEdit);
                actionPanel.add(btnDelete);

                lessonRow.add(textPanel, BorderLayout.CENTER);
                lessonRow.add(actionPanel, BorderLayout.EAST);
                panel.add(lessonRow);
                panel.add(Box.createVerticalStrut(5));
            }

            if (!hasLessons) {
                JLabel noLesson = new JLabel("  No lessons yet.");
                noLesson.setFont(new Font("Poppins", Font.ITALIC, 12));
                noLesson.setForeground(Color.GRAY);
                panel.add(noLesson);
            }

            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        panel.revalidate();
        panel.repaint();
    }

    //SHOW ENROLLED STUDENTS
    private void showEnrolledStudents(String courseID, String courseName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "SELECT s.student_ID, s.student_name, s.student_Year, " +
                         "s.student_Section, s.student_Contact, p.Program_Name, " +
                         "e.enrollment_Type, e.enrollment_Term " +
                         "FROM ENROLLMENT e " +
                         "JOIN STUDENT s ON e.student_ID = s.student_ID " +
                         "JOIN PROGRAM p ON s.program_ID = p.Program_ID " +
                         "WHERE e.course_ID = ? AND e.enrollment_Status = 'Enrolled' " +
                         "ORDER BY s.student_name";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, courseID);
            ResultSet rs = ps.executeQuery();

            boolean hasStudents = false;
            while (rs.next()) {
                hasStudents = true;

                JPanel studentCard = new JPanel(new GridLayout(0, 2, 5, 3));
                studentCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));

                //STUDENT INFORMATION DISPLAY
                studentCard.add(new JLabel("Student ID:"));
                studentCard.add(new JLabel(rs.getString("student_ID")));

                studentCard.add(new JLabel("Name:"));
                studentCard.add(new JLabel(rs.getString("student_name")));

                studentCard.add(new JLabel("Program:"));
                studentCard.add(new JLabel(rs.getString("Program_Name")));

                studentCard.add(new JLabel("Year:"));
                studentCard.add(new JLabel(rs.getString("student_Year")));

                studentCard.add(new JLabel("Section:"));
                studentCard.add(new JLabel(rs.getString("student_Section")));

                studentCard.add(new JLabel("Contact:"));
                studentCard.add(new JLabel(rs.getString("student_Contact")));

                studentCard.add(new JLabel("Type:"));
                studentCard.add(new JLabel(rs.getString("enrollment_Type")));

                studentCard.add(new JLabel("Term:"));
                studentCard.add(new JLabel(rs.getString("enrollment_Term")));

                panel.add(studentCard);
                panel.add(Box.createVerticalStrut(10));
            }

            //IF THERE ARE NO STUDENTS ENROLLED
            if (!hasStudents) {
                JLabel noStudents = new JLabel("No students enrolled in this course.");
                noStudents.setFont(new Font("Poppins", Font.ITALIC, 13));
                noStudents.setForeground(Color.GRAY);
                noStudents.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(noStudents);
            }

            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            panel.add(new JLabel("Failed to load students."));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(
            null, scrollPane,
            "Students in " + courseID + " - " + courseName,
            JOptionPane.PLAIN_MESSAGE
        );
    }

    //ADD LESSON ACTION
    private void showAddLessonDialog(String courseID, JPanel lessonsPanel) {
        JTextField tfTitle   = new JTextField();
        JTextArea  taContent = new JTextArea(4, 20);
        taContent.setLineWrap(true);
        taContent.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Lesson Title:"));
        form.add(tfTitle);
        form.add(new JLabel("Lesson Content:"));
        form.add(new JScrollPane(taContent));

        int result = JOptionPane.showConfirmDialog(
            null, form,
            "Add Lesson to " + courseID,
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String title   = tfTitle.getText().trim();
            String content = taContent.getText().trim();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Lesson title cannot be empty.");
                return;
            }
            insertLesson(courseID, title, content, lessonsPanel);
        }
    }

    //INSERT LESSON TO SQL
    private void insertLesson(String courseID, String title, String content, JPanel lessonsPanel) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String idSql = "SELECT COALESCE(MAX(lesson_ID), 0) + 1 AS next_ID FROM LESSON";
            PreparedStatement idPs = conn.prepareStatement(idSql);
            ResultSet idRs = idPs.executeQuery();
            int newID = 1;
            if (idRs.next()) newID = idRs.getInt("next_ID");

            String sql = "INSERT INTO LESSON (lesson_ID, course_ID, lesson_title, lesson_Content) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, newID);
            ps.setString(2, courseID);
            ps.setString(3, title);
            ps.setString(4, content.isEmpty() ? null : content);
            ps.executeUpdate();

            ps.close();
            idPs.close();
            conn.close();

            JOptionPane.showMessageDialog(null, "Lesson added successfully!");
            refreshLessons(lessonsPanel, courseID);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to add lesson.");
        }
    }


    //EDIT LESSON 
    private void showEditLessonDialog(int lessonID, String currentTitle, String currentContent, JPanel lessonsPanel, String courseID) {
        JTextField tfTitle = new JTextField(currentTitle);
        JTextArea  taContent = new JTextArea(currentContent != null ? currentContent : "", 4, 20);
        taContent.setLineWrap(true);
        taContent.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Lesson Title:"));
        form.add(tfTitle);
        form.add(new JLabel("Lesson Content:"));
        form.add(new JScrollPane(taContent));

        int result = JOptionPane.showConfirmDialog(
            null, form,
            "Edit Lesson",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String newTitle   = tfTitle.getText().trim();
            String newContent = taContent.getText().trim();

            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Lesson title cannot be empty.");
                return;
            }
            updateLesson(lessonID, newTitle, newContent, lessonsPanel, courseID);
        }
    }

    //UPDATE LESSON IN SQL
    private void updateLesson(int lessonID, String newTitle, String newContent, JPanel lessonsPanel, String courseID) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "UPDATE LESSON SET lesson_title = ?, lesson_Content = ? WHERE lesson_ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newTitle);
            ps.setString(2, newContent.isEmpty() ? null : newContent);
            ps.setInt(3, lessonID);
            ps.executeUpdate();

            ps.close();
            conn.close();

            JOptionPane.showMessageDialog(null, "Lesson updated successfully!");
            refreshLessons(lessonsPanel, courseID);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to update lesson.");
        }
    }

    //DELETE LESSON FROM SQL
    private void deleteLesson(int lessonID, JPanel lessonsPanel, String courseID) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "DELETE FROM LESSON WHERE lesson_ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, lessonID);
            ps.executeUpdate();

            ps.close();
            conn.close();

            JOptionPane.showMessageDialog(null, "Lesson deleted successfully!");
            refreshLessons(lessonsPanel, courseID);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to delete lesson.");
        }
    }
}