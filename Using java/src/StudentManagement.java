package src;

//Importing Required Libraries for GUI
import java.awt.*;
import java.sql.*;
import java.util.regex.*;
import javax.swing.*;

public class StudentManagement extends JFrame {
    //Define Component of GUI
    private JTextField queryField;
    private JButton processButton;
    private JTextArea resultArea;
    private Connection conn;

    public StudentManagement() {
        setTitle("Student Management System");
        setSize(650, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //Add BorderLayout to Frame
        setLayout(new BorderLayout());

        //Create Panel for Input
        JPanel inputPanel = new JPanel(new BorderLayout());

        //Field for Query
        queryField = new JTextField("Enter your query here:");
    
        //Set Font of Query Field
        queryField.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        //Set Background of Query Field
        queryField.setBackground(Color.decode("#D3D3D3"));

        //Button for Process Query
        processButton = new JButton("Process Query");

        //Add Components to Panel in Center
        inputPanel.add(queryField, BorderLayout.CENTER);
        //Add Button to Panel in East
        inputPanel.add(processButton, BorderLayout.EAST);

        //TextArea for Result
        resultArea = new JTextArea("Result will be displayed here:");

        //Set Font of Result TextArea
        resultArea.setFont(new Font("Cascadia Code", Font.PLAIN, 15));
        //Result TextArea is not Editable
        resultArea.setEditable(false);

        //Add Panel to Frame in North
        add(inputPanel, BorderLayout.NORTH);
        //Add ScrollPanel 
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        //Connect to Database call
        connectToDatabase();

        //Add Process Button Action Listener
        processButton.addActionListener(e -> processNaturalLanguageQuery());

        //Set Frame to Visible if true
        setVisible(true);
    }

    //Connect to Database Function
    private void connectToDatabase() {
        //try to connect database using database name,username and password
        try {
            //To Load MySQL Driver 
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Connect to Database
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbms_project", "root", "1234");
            JOptionPane.showMessageDialog(this, "Database Connected Successfully");
        }
        //if driver not found then catch 
        catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "MySQL Driver not found! Add mysql-connector-java.jar to classpath.");
            e.printStackTrace();
        } 
        //if connection failed then catch
        catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Function to Process Query
    private void processNaturalLanguageQuery() {
        //Get Query from 
        String userQuery = queryField.getText().trim().toLowerCase();
        //Check if Query is Empty
        String sqlQuery = null;
        //Get Roll Number from Query for Specific Student
        int rollNumber = extractNumber(userQuery);

        //Check for Specific Query
        if (userQuery.matches(".*\\b(all students|all records|show all)\\b.*")) {
            sqlQuery = "SELECT * FROM students";
        } 
        else if (Pattern.matches(".*\\b(print details of|show details of|give me details of) (\\d+)\\b.*", userQuery)) {
            sqlQuery = "SELECT * FROM students WHERE Roll_no = ?";
        } 
        else if (userQuery.matches(".*\\bcgpa of (\\d+)\\b.*")) {
            sqlQuery = "SELECT Roll_no, CGPA FROM students WHERE Roll_no = ?";
        } 
        else if (userQuery.matches(".*\\bhobby of (\\d+)\\b.*")) {
            sqlQuery = "SELECT Roll_no, Hobby FROM students WHERE Roll_no = ?";
        } 
        else if (userQuery.matches(".*\\bname of (\\d+)\\b.*")) {
            sqlQuery = "SELECT Roll_no, Name FROM students WHERE Roll_no = ?";
        } 
        else if (userQuery.matches(".*\\bbranch of (\\d+)\\b.*")) {
            sqlQuery = "SELECT Roll_no, Branch FROM students WHERE Roll_no = ?";
        } 
        else if (userQuery.matches(".*\\bsem of (\\d+)\\b.*")) {
            sqlQuery = "SELECT Roll_no, Sem FROM students WHERE Roll_no = ?";
        } 
        //If Query is not matched of upper query then show error message
        else {
            resultArea.setText("⚠️ Sorry, I couldn't understand your query.");
            return;
        }

        //Call Function Execute Query 
        executeQuery(sqlQuery, rollNumber);
    }

    //Function to Extract Number from Query
    private int extractNumber(String text) {
        Matcher matcher = Pattern.compile("(\\d+)").matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    //Function of Execute Query
    private void executeQuery(String sqlQuery, int rollNumber) {
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            //Check if Query Contains WHERE Roll_no = ?
            if (sqlQuery.contains("WHERE Roll_no = ?")) {
                stmt.setInt(1, rollNumber);
            }
            //Execute Query
            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder result = new StringBuilder();

                //Get MetaData of ResultSet
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                //Loop through all records
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        result.append(metaData.getColumnName(i)).append(": ").append(rs.getString(i)).append("\n");
                    }
                    result.append("\n");
                }
                //print Result to TextArea in Frame
                resultArea.setText(result.length() > 0 ? result.toString() : "No records found.");
            }
        } 
        //Catch execute if Query Execution Failed
        catch (SQLException e) {
            resultArea.setText("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Main Function
    public static void main(String[] args) {
        new StudentManagement();
    }
}
