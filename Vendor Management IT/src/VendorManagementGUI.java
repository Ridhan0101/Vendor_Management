import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import com.itextpdf.text.Image;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
// import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VendorManagementGUI {
    private static final String URL = "jdbc:postgresql://localhost:5432/Data_Vendor_PT_DI";
    private static final String USER = "postgres";
    private static final String PASSWORD = "anka9904";
    private static JRadioButton calonVendorRadioButton;
    private static JRadioButton vendorRadioButton;
    private static JComboBox<String> vendorDropdown;
    private static JLabel alamatLabel;
    private static JTable layananTable;
    private static JComboBox<String> kategoriDropdown;
    private static JTable analisaVendorReportTable;
    private static JComboBox<String> kategoriReportAnalisaVendorDropdown;
    private static DefaultTableModel analisaVendorReportTableModel = new DefaultTableModel();
    private static JButton cetakPdfButton;
    private static JTextField vendorNameTextField = new JTextField();
    private static JTextField serviceNameTextField = new JTextField();
    private static JComboBox<String> vendorReportComboBox;
    private static JTable slaTable;
    private static JTable slaTable1;
    private static Connection connection;
    private static final String COMPANY_NAME = "PT Dirgantara Indonesia";
    private static final String COMPANY_ADDRESS = "Jalan Pajajaran No. 154 Bandung 4017 West Java - Indonesia";
    private static JTable ratingTable = new JTable();
    private static final String LOGO_PATH = "icon PT.png";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        // Use the Event Dispatch Thread (EDT) for Swing components
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Vendor Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setIconImage(new ImageIcon("path/to/icon.png").getImage());

        ImageIcon icon = new ImageIcon("icon PT.png"); // Ganti dengan path lengkap ke ikon Anda
        frame.setIconImage(icon.getImage());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab Vendor
        JPanel vendorPanel = createVendorPanel();
        tabbedPane.addTab("Tambah Vendor", vendorPanel);

        // Tab Kategori
        JPanel kategoriPanel = createKategoriPanel();
        tabbedPane.addTab("Tambah Kategori", kategoriPanel);

        // Panel Analisa Vendor
        JPanel AnalisaPanel = createAnalisaVendorPanel();
        tabbedPane.addTab("Analisa Vendor", AnalisaPanel);

        // Panel Analisa Report Vendor
        JPanel ReportPanel = createReportanalisaPanel();
        tabbedPane.addTab("Report Analisa Vendor", ReportPanel);

        // Panel Table Vendors
        JPanel TableVendorPanel = CreateTableVendors();
        tabbedPane.addTab("Vendor", TableVendorPanel);

        // Panel Input SLA
        JPanel SLApPanel = createSLAPanel();
        tabbedPane.addTab("SLA Vendor", SLApPanel);

        // panel Report SLA
        JPanel reportPanel = createReportSLAPanel();
        tabbedPane.addTab("Report SLA Vendor", reportPanel);

        frame.getContentPane().add(tabbedPane);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }

    private static Object[][] getDataFromDatabaseSla(String selectedVendor) {
        List<Object[]> slaDataList = new ArrayList<>();

        // Assuming you have a connected database connection passed as a parameter
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement statement = connection
                    .prepareStatement("SELECT sla_details FROM sla WHERE vendor = ?")) {
                statement.setString(1, selectedVendor);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    // Assuming "sla_details" is the correct column name for SLA in your database
                    String slaValue = resultSet.getString("sla_details");
                    slaDataList.add(new Object[] { slaValue });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        // Convert the list to a 2D array
        Object[][] slaDataArray = new Object[slaDataList.size()][1];
        for (int i = 0; i < slaDataList.size(); i++) {
            slaDataArray[i] = slaDataList.get(i);
        }

        return slaDataArray;
    }

    private static Object[][] getDataFromDatabaseLayanan(String selectedVendor) {
        List<Object[]> layananDataList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement statement = connection
                    .prepareStatement("SELECT layanan FROM analisa WHERE nama_vendor = ?")) {
                statement.setString(1, selectedVendor);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    // Assuming "layanan" is the correct column name for layanan in your database
                    String layananValue = resultSet.getString("layanan");
                    layananDataList.add(new Object[] { layananValue });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        // Convert the list to a 2D array
        Object[][] layananDataArray = new Object[layananDataList.size()][1];
        for (int i = 0; i < layananDataList.size(); i++) {
            layananDataArray[i] = layananDataList.get(i);
        }

        return layananDataArray;
    }

    private static Object[][] getDataFromDatabaseHasilKinerja(String selectedVendor) {
        // Replace this with your actual database connection and query logic
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(); // Implement the getConnection() method or use your existing connection logic

            // Define the SQL query to retrieve performance data based on the selectedVendor
            String query = "SELECT data_kinerja FROM hasil_kinerja WHERE vendor = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, selectedVendor);

            // Execute the query
            resultSet = preparedStatement.executeQuery();

            // Process the result set and convert it into a 2D array
            List<Object[]> resultList = new ArrayList<>();
            while (resultSet.next()) {
                // Assuming there's a column named 'hasil_kinerja' in your database
                String hasilKinerja = resultSet.getString("data_kinerja");
                resultList.add(new Object[] { hasilKinerja });
            }

            // Convert the list to a 2D array
            Object[][] resultArray = new Object[resultList.size()][1];
            resultList.toArray(resultArray);

            return resultArray;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database errors
        } finally {
            // Close database resources
            try {
                if (resultSet != null)
                    resultSet.close();
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Return an empty array if no data is found or an error occurs
        return new Object[][] {};
    }

    private static Object[][] getDataFromDatabaseRating(String selectedVendor) {
        // Replace this with your actual database connection and query logic
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            // Define the SQL query to retrieve rating data based on the selectedVendor
            String query = "SELECT rating FROM hasil_kinerja WHERE vendor = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, selectedVendor);

            // Execute the query
            resultSet = preparedStatement.executeQuery();

            // List to store rating data
            List<Object[]> ratingDataList = new ArrayList<>();

            // Loop through the result set and add data to the list
            while (resultSet.next()) {
                int rating = resultSet.getInt("rating");
                ratingDataList.add(new Object[] { rating });
            }

            // Convert the list to a 2D array for table display
            Object[][] ratingData = new Object[ratingDataList.size()][1];
            for (int i = 0; i < ratingDataList.size(); i++) {
                ratingData[i] = ratingDataList.get(i);
            }

            return ratingData;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database errors
        } finally {
            // Close database resources
            try {
                if (resultSet != null)
                    resultSet.close();
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Return null if no data is found or an error occurs
        return null;
    }

    private static void refreshData(JTable layananTable, JTable slaTable, JComboBox<String> vendorReportComboBox) {
        // Get the selected vendor
        String selectedVendor = (String) vendorReportComboBox.getSelectedItem();

        if (selectedVendor != null) {
            // Fetch and update data for the selected vendor from the database
            Object[][] refreshedDataLayanan = getDataFromDatabaseLayanan(selectedVendor);
            Object[][] refreshedDataSla = getDataFromDatabaseSla(selectedVendor);

            // Get the existing table models
            DefaultTableModel layananTableModel = (DefaultTableModel) layananTable.getModel();
            DefaultTableModel slaTableModel = (DefaultTableModel) slaTable.getModel();

            // Remove existing rows from the table models
            layananTableModel.setRowCount(0);
            slaTableModel.setRowCount(0);

            // Add new data to the table models
            for (Object[] row : refreshedDataLayanan) {
                layananTableModel.addRow(row);
            }

            for (Object[] row : refreshedDataSla) {
                slaTableModel.addRow(row);
            }

            // Notify that data has been refreshed
            JOptionPane.showMessageDialog(null, "Data di refresh untuk " + selectedVendor, "Refresh Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refresh the combo box with updated vendor names
            refreshVendorComboBox(vendorReportComboBox);
        }
    }

    private static void refreshVendorComboBox(JComboBox<String> vendorReportComboBox) {
        // Fetch and update vendor names from the database
        // Assuming you have a method to populate the vendor combo box
        populateVendorComboBox(vendorReportComboBox);
    }

    private static JPanel createReportSLAPanel() {
        // Main panel with BorderLayout
        JPanel panel = new JPanel(new BorderLayout());

        // Panel for the top part (drop-down and buttons)
        JPanel topPanel = new JPanel();
        JLabel vendorLabel = new JLabel("Pilih Vendor:");
        JComboBox<String> vendorReportComboBox = new JComboBox<>();
        populateVendorComboBox(vendorReportComboBox); // Assuming you have a method to populate the combo box
        JButton showReportButton = new JButton("Tampilkan Report");
        JButton printPdfButton = new JButton("Cetak PDF Report SLA Vendor");
        JButton refreshButton = new JButton("Refresh Data");
        JButton addPerformanceButton = new JButton("Tambah Hasil Kinerja");
        JLabel averageRatingLabel = new JLabel("Rating Vendor:  ");
        topPanel.add(vendorLabel);
        topPanel.add(vendorReportComboBox);
        topPanel.add(showReportButton);
        topPanel.add(addPerformanceButton);
        topPanel.add(averageRatingLabel);

        // Panel for the bottom part (tables and "Cetak PDF" button)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        // "Layanan" table
        JTable layananTable = new JTable();
        JScrollPane layananScrollPane = new JScrollPane(layananTable);
        bottomPanel.add(layananScrollPane);

        // "SLA" table
        JTable slaTable = new JTable();
        JScrollPane slaScrollPane = new JScrollPane(slaTable);
        bottomPanel.add(slaScrollPane);

        // "Hasil Kinerja" table
        JTable hasilKinerjaTable = new JTable();
        JScrollPane hasilKinerjaScrollPane = new JScrollPane(hasilKinerjaTable);
        bottomPanel.add(hasilKinerjaScrollPane);

        // Add the "Cetak PDF" button
        JPanel bottomLowPanel = new JPanel();
        bottomLowPanel.add(printPdfButton);
        bottomLowPanel.add(refreshButton);

        bottomPanel.add(bottomLowPanel);

        // Add the top and bottom panels to the main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        // Add action listener for the "Tampilkan Report" button
        showReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedVendor = (String) vendorReportComboBox.getSelectedItem();
                if (selectedVendor != null) {
                    displayReport(selectedVendor, layananTable, slaTable, hasilKinerjaTable, hasilKinerjaTable);
                    updateAverageRatingLabel(selectedVendor, ratingTable, averageRatingLabel);
                }
            }

            private void updateAverageRatingLabel(String selectedVendor, JTable ratingTable,
                    JLabel averageRatingLabel) {
                // Fetch the rating data from the database
                Object[][] dataRating = getDataFromDatabaseRating(selectedVendor);

                // Check if there is rating data
                if (dataRating != null && dataRating.length > 0) {
                    // Calculate the average rating from the fetched data
                    double averageRating = calculateAverageRating(dataRating);

                    // Convert the average numeric rating to label
                    String averageRatingLabelString = convertNumericRatingToLabel((int) Math.round(averageRating));

                    // Update the label with the calculated average rating
                    averageRatingLabel.setText("Rata-rata Rating: " + averageRatingLabelString);
                } else {
                    // No rating data, set the average rating label to "Unknown"
                    averageRatingLabel.setText("Rata-rata Rating: Unknown");
                }
            }

            // Add this method to convert numeric rating to label
            private String convertNumericRatingToLabel(int numericRating) {
                switch (numericRating) {
                    case 1:
                        return "Tidak Memuaskan";
                    case 2:
                        return "Memuaskan";
                    case 3:
                        return "Sangat Memuaskan";
                    default:
                        return "Unknown"; // Handle unexpected numeric ratings
                }
            }

            private double calculateAverageRating(Object[][] dataRating) {
                int rowCount = dataRating.length;

                if (rowCount == 0) {
                    return 0; // Return 0 if there are no ratings
                }

                int totalRating = 0;

                // Sum up all the numeric ratings in the table
                for (int i = 0; i < rowCount; i++) {
                    Object ratingObject = dataRating[i][0];

                    if (ratingObject instanceof Integer) {
                        // If it's already an Integer, no need to convert
                        totalRating += (int) ratingObject;
                    } else if (ratingObject instanceof String) {
                        // If it's a String, convert it to an Integer
                        String ratingLabel = (String) ratingObject;
                        int numericRating = convertLabelToNumericRating(ratingLabel);
                        totalRating += numericRating;
                    }
                }

                // Calculate the average rating
                double averageRating = (double) totalRating / rowCount;

                return averageRating;
            }

            private int convertLabelToNumericRating(String ratingLabel) {
                try {
                    return Integer.parseInt(ratingLabel.trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                return 0; // Handle unexpected values
            }

            private void displayReport(String selectedVendor, JTable layananTable, JTable slaTable,
                    JTable hasilKinerjaTable, JTable ratingTable) {
                // Fetch data from the database
                Object[][] dataLayanan = getDataFromDatabaseLayanan(selectedVendor);
                Object[][] dataSla = getDataFromDatabaseSla(selectedVendor);
                Object[][] dataHasilKinerja = getDataFromDatabaseHasilKinerja(selectedVendor);
                Object[][] dataRating = getDataFromDatabaseRating(selectedVendor);

                // Columns for the tables
                String[] columns = { "Layanan" };
                String[] columns1 = { "SLA" };
                String[] ratingColumns = { "Hasil Kinerja", "Rating" }; // Updated the column names

                // Define getQualityLabel method
                Function<Integer, String> getQualityLabel = rating -> {
                    switch (rating) {
                        case 1:
                            return "Tidak Memuaskan";
                        case 2:
                            return "Memuaskan";
                        case 3:
                            return "Sangat Memuaskan";
                        default:
                            return "Unknown"; // Handle unexpected values
                    }
                };

                // Create new table models with data from the database
                DefaultTableModel layananTableModel = new DefaultTableModel(dataLayanan, columns);
                DefaultTableModel slaTableModel = new DefaultTableModel(dataSla, columns1);

                // Combine "Hasil Kinerja" and "Rating" into a single table model
                DefaultTableModel hasilKinerjaRatingTableModel = new DefaultTableModel(null, ratingColumns);
                for (int i = 0; i < Math.max(dataHasilKinerja.length, dataRating.length); i++) {
                    Object hasilKinerja = (i < dataHasilKinerja.length) ? dataHasilKinerja[i][0] : "";
                    Object rating = (i < dataRating.length) ? getQualityLabel.apply((int) dataRating[i][0]) : "";
                    hasilKinerjaRatingTableModel.addRow(new Object[] { hasilKinerja, rating });
                }

                // Set table models
                layananTable.setModel(layananTableModel);
                slaTable.setModel(slaTableModel);
                hasilKinerjaTable.setModel(hasilKinerjaRatingTableModel);
            }

        });

        addPerformanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedVendor = (String) vendorReportComboBox.getSelectedItem();
                if (selectedVendor != null) {
                    showAddPerformanceDialog(selectedVendor, hasilKinerjaTable, ratingTable);
                }
            }

            private void showAddPerformanceDialog(String selectedVendor, JTable hasilKinerjaTable, JTable ratingTable) {
                JTextField performanceField = new JTextField();
                JComboBox<String> ratingComboBox = new JComboBox<>(
                        new String[] { " ", "Tidak Memuaskan", "Memuaskan", "Sangat Memuaskan" });

                JPanel panel = new JPanel(new GridLayout(0, 1));
                panel.add(new JLabel("Hasil Kinerja:"));
                panel.add(performanceField);
                panel.add(new JLabel("Rating:"));
                panel.add(ratingComboBox);

                int result = JOptionPane.showConfirmDialog(null, panel, "Tambah Hasil Kinerja",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String performanceResult = performanceField.getText();
                    String selectedRating = (String) ratingComboBox.getSelectedItem();

                    if (!performanceResult.isEmpty() && selectedRating != null) {
                        // Inlined logic for converting label to numeric rating
                        int numericRating;
                        switch (selectedRating) {
                            case "Tidak Memuaskan":
                                numericRating = 1;
                                break;
                            case "Memuaskan":
                                numericRating = 2;
                                break;
                            case "Sangat Memuaskan":
                                numericRating = 3;
                                break;
                            default:
                                numericRating = 0; // Handle unexpected values
                        }

                        // Add performance result to the "Hasil Kinerja" table
                        addPerformanceResultToTable(selectedVendor, performanceResult, numericRating,
                                hasilKinerjaTable, ratingTable);

                        // Add rating to the "Rating" table
                        addRatingToTable(numericRating, ratingTable);

                        // Save data to the database
                        savePerformanceDataToDatabase(selectedVendor, performanceResult, numericRating);

                        // Display success message
                        JOptionPane.showMessageDialog(null, "Data Hasil Kinerja berhasil ditambahkan!", "Sukses",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

            private void addRatingToTable(int rating, JTable ratingTable) {
                DefaultTableModel ratingTableModel = (DefaultTableModel) ratingTable.getModel();
                ratingTableModel.addRow(new Object[] { rating });
            }

            private void addPerformanceResults(String selectedVendor, JTable hasilKinerjaTable, JTable ratingTable) {
                String performanceResult = showPerformanceInputDialog();

                if (performanceResult != null && !performanceResult.isEmpty()) {
                    // Menyesuaikan hasil kinerja berdasarkan kata kunci
                    int calculatedRating = calculateRatingFromPerformance(performanceResult);

                    // Menambahkan hasil kinerja ke tabel
                    addPerformanceResultToTable(selectedVendor, performanceResult, calculatedRating, hasilKinerjaTable,
                            ratingTable);

                    // Menyimpan data hasil kinerja ke database
                    savePerformanceDataToDatabase(selectedVendor, performanceResult, calculatedRating);
                }
            }

            private int calculateRatingFromPerformance(String performanceResult) {
                // Menyesuaikan hasil kinerja berdasarkan kata kunci
                if (performanceResult.toLowerCase().contains("tidak berhasil")) {
                    return 1; // Tidak Memuaskan
                } else if (performanceResult.toLowerCase().contains("kurang")) {
                    return 2; // Memuaskan
                } else {
                    return 3; // Sangat Memuaskan (atau rating lain sesuai kebutuhan)
                }
            }

            private String showPerformanceInputDialog() {
                return JOptionPane.showInputDialog(null, "Masukkan Hasil Kinerja:", "Tambah Hasil Kinerja",
                        JOptionPane.PLAIN_MESSAGE);
            }

            private void addPerformanceResultToTable(String selectedVendor, String performanceResult, int rating,
                    JTable hasilKinerjaTable, JTable ratingTable) {
                // Assuming your table models are DefaultTableModel
                DefaultTableModel hasilKinerjaModel = (DefaultTableModel) hasilKinerjaTable.getModel();
                DefaultTableModel ratingModel = (DefaultTableModel) ratingTable.getModel();

                // Combine the performance result and quality label into a single string
                String resultWithRating = performanceResult + " (Rating: " + getQualityLabel(rating) + ")";

                // Add a new row to the "Hasil Kinerja" table with the combined information
                hasilKinerjaModel.addRow(new Object[] { resultWithRating });

                // Add a new row to the "Rating" table with the text label
                ratingModel.addRow(new Object[] { getQualityLabel(rating) });
            }

            private String getQualityLabel(int rating) {
                // Determine the quality label based on the numeric rating
                switch (rating) {
                    case 1:
                        return "Tidak Memuaskan";
                    case 2:
                        return "Memuaskan";
                    case 3:
                        return "Sangat Memuaskan";
                    default:
                        return "Unknown"; // Handle unexpected values
                }
            }

            private void savePerformanceDataToDatabase(String selectedVendor, String performanceResult, int rating) {
                // You need to implement this method to save performance data to the database
                // For example, use JDBC to execute an SQL INSERT statement
                Connection connection = null;
                PreparedStatement preparedStatement = null;

                try {
                    connection = getConnection();
                    // Define the SQL query to insert performance data into the database
                    String query = "INSERT INTO Hasil_Kinerja (vendor, Data_Kinerja, Rating) VALUES (?, ?, ?)";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, selectedVendor);
                    preparedStatement.setString(2, performanceResult);
                    preparedStatement.setInt(3, rating);

                    // Execute the query
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    // Handle database errors
                    JOptionPane.showMessageDialog(null, "Error saving performance data to the database.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Close database resources
                    try {
                        if (preparedStatement != null)
                            preparedStatement.close();
                        if (connection != null)
                            connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData(layananTable, slaTable, vendorReportComboBox);
            }
        });

        // Add action listener for the "Cetak PDF" button
        printPdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedVendor = (String) vendorReportComboBox.getSelectedItem();
                if (selectedVendor != null) {
                    // Fetch data from the database
                    Object[][] dataLayanan = getDataFromDatabaseLayanan(selectedVendor);
                    Object[][] dataSla = getDataFromDatabaseSla(selectedVendor);

                    // Check if there is data
                    if (dataLayanan.length == 0 && dataSla.length == 0) {
                        // No data available, display a message
                        JOptionPane.showMessageDialog(null, "Tidak ada data yang tersedia untuk dibuatkan PDF.", "Info",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Panggil fungsi untuk membuat dan menyimpan file PDF
                        createAndSavePdfReport(selectedVendor, dataLayanan, dataSla);
                    }
                }
            }

            private String convertNumericRatingToLabel(double numericRating) {
                if (numericRating >= 1.0 && numericRating <= 1.5) {
                    return "Tidak Memuaskan";
                } else if (numericRating > 1.5 && numericRating <= 2.5) {
                    return "Memuaskan";
                } else if (numericRating > 2.5 && numericRating <= 3.0) {
                    return "Sangat Memuaskan";
                } else {
                    return "Unknown"; // Handle unexpected numeric ratings
                }
            }

            private void createAndSavePdfReport(String selectedVendor, Object[][] dataLayanan, Object[][] dataSla) {
                Document document = new Document();
                try {
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    String formattedDateTime = currentDateTime.format(dateTimeFormatter);

                    // Set document metadata
                    document.addTitle("SLA Report for Vendor: " + selectedVendor);
                    document.addSubject("Service Level Agreement Report");

                    // Specify the location to save the PDF file
                    String pdfFilePath = "Report_" + selectedVendor + "_SLA.pdf";
                    PdfWriter.getInstance(document, new FileOutputStream(pdfFilePath));

                    document.open();

                    // Add a colorful and artistic header with logo
                    PdfPTable headerTable = new PdfPTable(2); // Menggunakan 2 kolom untuk header dan logo
                    headerTable.setWidthPercentage(100);

                    // Tambahkan sel untuk logo
                    Image logo = Image.getInstance(LOGO_PATH);
                    logo.scaleToFit(20, 20);

                    PdfPCell logoCell = new PdfPCell(logo, true);
                    logoCell.setFixedHeight(40); // Set the fixed height of the cell to limit the image height
                    logoCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Adjust alignment if necessary
                    headerTable.addCell(logoCell);

                    // Tambahkan sel untuk judul header
                    PdfPCell titleCell = new PdfPCell();
                    // titleCell.setBorder(Rectangle.NO_BORDER);
                    com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24,
                            new BaseColor(70, 130, 180)); // Steel Blue
                    titleCell.addElement(new Paragraph("SLA Vendor Management IT Report", titleFont));
                    headerTable.addCell(titleCell);

                    // Tambahkan headerTable ke dokumen
                    document.add(headerTable);

                    // Fetch vendor information including phone number and address from the database
                    VendorData vendorData = getVendorData(selectedVendor);

                    // Add company information and vendor details
                    com.itextpdf.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                    Paragraph vendorInfo = new Paragraph();
                    vendorInfo.add(new Paragraph(COMPANY_NAME, infoFont));
                    vendorInfo.add(new Paragraph("Alamat Perusahaan: " + COMPANY_ADDRESS, infoFont));
                    vendorInfo.add(new Paragraph("Nama Vendor: " + selectedVendor, infoFont));
                    vendorInfo.add(new Paragraph("Nomor Telepon Vendor: " + vendorData.getPhoneNumber(), infoFont));
                    vendorInfo.add(new Paragraph("Alamat Vendor: " + vendorData.getalamat(), infoFont));
                    vendorInfo.setAlignment(Element.ALIGN_CENTER);
                    document.add(vendorInfo);

                    // Add date and time to the PDF
                    document.add(new Paragraph("Tanggal Cetak: " + formattedDateTime, infoFont));

                    // Fetch rating data from the database
                    Object[][] dataRating = getDataFromDatabaseRating(selectedVendor);

                    // Calculate the average rating
                    double averageRating = calculateAverageRating(dataRating);

                    // Convert average rating to label
                    String averageRatingLabel = convertNumericRatingToLabel(averageRating);

                    // Add average rating and label to the PDF
                    com.itextpdf.text.Font ratingFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD,
                            BaseColor.BLACK);
                    Paragraph ratingParagraph = new Paragraph("Rating Vendor: " + averageRatingLabel, ratingFont);
                    ratingParagraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(ratingParagraph);

                    // Create and add table for Layanan
                    PdfPTable layananTable = new PdfPTable(1);
                    layananTable.setWidthPercentage(80);
                    layananTable.setSpacingBefore(20f);
                    layananTable.getDefaultCell().setPadding(8);
                    addTableHeader(layananTable, "Layanan");

                    // Add data from the database to the Layanan table with alternating row colors
                    for (int i = 0; i < dataLayanan.length; i++) {
                        PdfPCell cell = new PdfPCell(new Phrase(dataLayanan[i][0].toString()));
                        cell.setBackgroundColor(i % 2 == 0 ? BaseColor.LIGHT_GRAY : BaseColor.WHITE);
                        layananTable.addCell(cell);
                    }
                    document.add(layananTable);

                    // Create and add table for SLA
                    PdfPTable slaTable = new PdfPTable(1);
                    slaTable.setWidthPercentage(80);
                    slaTable.setSpacingBefore(20f);
                    slaTable.getDefaultCell().setPadding(8);
                    addTableHeader(slaTable, "SLA");

                    // Add data from the database to the SLA table (replace with actual data)
                    addTableData(slaTable, dataSla);
                    document.add(slaTable);

                    // Create and add table for Hasil Kinerja
                    PdfPTable hasilKinerjaTable = new PdfPTable(1);
                    hasilKinerjaTable.setWidthPercentage(80);
                    hasilKinerjaTable.setSpacingBefore(20f);
                    hasilKinerjaTable.getDefaultCell().setPadding(8);
                    addTableHeader(hasilKinerjaTable, "Hasil Kinerja");

                    // Add data from the database to the Hasil Kinerja table (replace with actual
                    // data)
                    Object[][] hasilKinerjaData = getDataFromDatabaseHasilKinerja(selectedVendor);
                    addTableData(hasilKinerjaTable, hasilKinerjaData);
                    document.add(hasilKinerjaTable);

                    // Display success message using JOptionPane
                    JOptionPane.showMessageDialog(null, "File PDF berhasil dibuat dan ditutup: " + pdfFilePath);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    // Display error message using JOptionPane
                    JOptionPane.showMessageDialog(null, "Error creating PDF: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Close resources properly
                    if (document != null && document.isOpen()) {
                        document.close();
                    }
                }
            }

            private double calculateAverageRating(Object[][] dataRating) {
                int rowCount = dataRating.length;

                if (rowCount == 0) {
                    return 0; // Return 0 if there are no ratings
                }

                int totalRating = 0;

                // Sum up all the numeric ratings in the table
                for (int i = 0; i < rowCount; i++) {
                    Object ratingObject = dataRating[i][0];

                    if (ratingObject instanceof Integer) {
                        // If it's already an Integer, no need to convert
                        totalRating += (int) ratingObject;
                    } else if (ratingObject instanceof String) {
                        // If it's a String, convert it to an Integer
                        String ratingLabel = (String) ratingObject;
                        int numericRating = convertLabelToNumericRating(ratingLabel);
                        totalRating += numericRating;
                    }
                }

                // Calculate the average rating
                return (double) totalRating / rowCount;
            }

            private int convertLabelToNumericRating(String ratingLabel) {
                try {
                    return Integer.parseInt(ratingLabel.trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                return 0; // Handle unexpected values
            }

            private void addTableHeader(PdfPTable table, String header) {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                cell.setBackgroundColor(BaseColor.GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            private void addTableData(PdfPTable table, Object[][] data) {
                for (Object[] row : data) {
                    table.addCell(row[0].toString());
                }
            }

            private VendorData getVendorData(String selectedVendor) {
                // Replace this with your actual database connection and query logic
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;

                try {
                    connection = getConnection();
                    // Define the SQL query to retrieve vendor data based on the selectedVendor
                    String query = "SELECT hp, kota FROM vendors WHERE nama_vendor = ?";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, selectedVendor);

                    // Execute the query
                    resultSet = preparedStatement.executeQuery();

                    // Check if a record is found
                    if (resultSet.next()) {
                        // Retrieve vendor data from the result set
                        String phoneNumber = resultSet.getString("hp");
                        String address = resultSet.getString("kota");

                        // Create and return a VendorData object
                        return new VendorData(address, phoneNumber);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Handle database errors
                } finally {
                    // Close database resources
                    try {
                        if (resultSet != null)
                            resultSet.close();
                        if (preparedStatement != null)
                            preparedStatement.close();
                        if (connection != null)
                            connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                // Return null if no data is found or an error occurs
                return null;
            }

            class VendorData {
                private String alamat;
                private String phoneNumber;

                public VendorData(String alamat, String phoneNumber) {
                    this.alamat = alamat;
                    this.phoneNumber = phoneNumber;
                }

                public String getalamat() {
                    return alamat;
                }

                public String getPhoneNumber() {
                    return phoneNumber;
                }
            }
        });

        return panel;
    }

    private static JPanel createSLAPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("SLA Entry")); // Memberikan border berjudul

        JPanel formPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add SLA");
        formPanel.add(addButton);
        styleButton(addButton);

        JButton editButton = new JButton("Edit SLA");
        formPanel.add(editButton);
        styleButton(editButton);

        panel.add(formPanel, BorderLayout.NORTH);

        String[] columnNames = { "Vendor", "SLA Details" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable slaTable = new JTable(tableModel);
        styleTable(slaTable);

        JScrollPane scrollPane = new JScrollPane(slaTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        populateSLATableFromDatabase(tableModel);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the add SLA dialog
                addSLADialog(scrollPane, tableModel);
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = slaTable.getSelectedRow();
                if (selectedRow != -1) {
                    String selectedVendor = (String) slaTable.getValueAt(selectedRow, 0);
                    String selectedSLADetails = (String) slaTable.getValueAt(selectedRow, 1);

                    // Dialog untuk edit SLA
                    editSLADialog(selectedVendor, selectedSLADetails, tableModel);
                } else {
                    JOptionPane.showMessageDialog(panel, "Pilih data SLA yang akan diedit", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private static void styleButton(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new java.awt.Color(60, 179, 113)); // Warna saat hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UIManager.getColor("control")); // Warna normal
            }
        });
    }

    private static void styleTable(JTable table) {
        table.setBackground(new java.awt.Color(240, 248, 255)); // Warna latar belakang tabel
        table.setSelectionBackground(new java.awt.Color(173, 216, 230)); // Warna saat baris dipilih
        table.setGridColor(new java.awt.Color(192, 192, 192)); // Warna garis batas
    }

    private static void addSLADialog(Component parentComponent, DefaultTableModel tableModel) {
        JDialog addDialog = new JDialog();
        addDialog.setTitle("Add SLA");

        JLabel vendorLabel = new JLabel("Vendor:");
        JComboBox<String> vendorComboBox = new JComboBox<>();
        populateVendorComboBox(vendorComboBox);

        JLabel slaLabel = new JLabel("SLA Details:");
        JTextField slaTextField = new JTextField();

        JButton addButton = new JButton("Add");
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(vendorLabel);
        inputPanel.add(vendorComboBox);
        inputPanel.add(slaLabel);
        inputPanel.add(slaTextField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);

        addDialog.setLayout(new BorderLayout());
        addDialog.add(inputPanel, BorderLayout.CENTER);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            String selectedVendor = (String) vendorComboBox.getSelectedItem();
            String slaDetails = slaTextField.getText().trim();

            if (selectedVendor != null && !slaDetails.isEmpty()) {
                saveSLAToDatabase(selectedVendor, slaDetails);
                JOptionPane.showMessageDialog(addDialog, "SLA berhasil ditambahkan!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    tableModel.addRow(new Object[] { selectedVendor, slaDetails });
                });

                addDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(addDialog, "Semua Kolom wajib diisi!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        addDialog.pack();
        addDialog.setSize(400, 200);
        addDialog.setLocationRelativeTo(parentComponent);
        addDialog.setVisible(true);
    }

    private static void editSLADialog(String vendor, String slaDetails, DefaultTableModel tableModel) {
        JDialog editDialog = new JDialog();
        editDialog.setTitle("Edit SLA");

        JLabel vendorLabel = new JLabel("Vendor:");
        JTextField vendorTextField = new JTextField(vendor);
        vendorTextField.setEditable(false);

        JLabel slaLabel = new JLabel("SLA Details:");
        JTextField slaTextField = new JTextField(slaDetails);

        JButton saveButton = new JButton("Save");
        JPanel inputPanel = new JPanel(new GridLayout(3, 2)); // Increase the rows for a larger form
        inputPanel.add(vendorLabel);
        inputPanel.add(vendorTextField);
        inputPanel.add(slaLabel);
        inputPanel.add(slaTextField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);

        editDialog.setLayout(new BorderLayout());
        editDialog.add(inputPanel, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        editDialog.setIconImage(new ImageIcon("edit.png").getImage());

        saveButton.addActionListener(e -> {
            String newSLADetails = slaTextField.getText().trim();
            if (!newSLADetails.isEmpty()) {
                // Find the selected row based on both vendor and SLA details
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String tableVendor = (String) tableModel.getValueAt(i, 0);
                    String tableSLADetails = (String) tableModel.getValueAt(i, 1);

                    if (tableVendor.equals(vendor) && tableSLADetails.equals(slaDetails)) {
                        tableModel.setValueAt(newSLADetails, i, 1);

                        JOptionPane.showMessageDialog(editDialog, "SLA Berhasil diedit!!!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        editDialog.dispose();
                        return; // Exit the loop once the row is found and updated
                    }
                }

                // If the loop completes and the row is not found
                JOptionPane.showMessageDialog(editDialog, "Data vendor tidak ditemukan", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(editDialog, "Mohon isi SLA Details", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Increase the size of the dialog for a larger form
        editDialog.pack();
        editDialog.setSize(400, 200); // Adjust the size according to your preference
        editDialog.setLocationRelativeTo(null);
        editDialog.setVisible(true);
    }

    private static void populateSLATableFromDatabase(DefaultTableModel tableModel) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT vendor, sla_details FROM sla")) {
                ResultSet resultSet = statement.executeQuery();

                // Bersihkan model tabel sebelum menambahkan data baru
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                });

                while (resultSet.next()) {
                    String vendor = resultSet.getString("vendor");
                    String slaValue = resultSet.getString("sla_details");

                    // Tambahkan data baru ke model tabel
                    SwingUtilities.invokeLater(() -> {
                        tableModel.addRow(new Object[] { vendor, slaValue });
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void populateVendorComboBox(JComboBox<String> comboBox) {
        List<String> vendors = fetchVendorsFromDatabase();
        for (String vendor : vendors) {
            comboBox.addItem(vendor);
        }
    }

    private static List<String> fetchVendorsFromDatabase() {
        List<String> vendors = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT nama_vendor FROM vendors where status= 'Vendor'";
            try (PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String vendorName = resultSet.getString("nama_vendor");
                    vendors.add(vendorName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vendors;
    }

    private static void saveSLAToDatabase(String vendor, String slaDetails) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO sla (vendor, sla_details) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, vendor);
                statement.setString(2, slaDetails);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static JPanel createReportanalisaPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Fetch vendors from the database
        List<String> vendorNames = getVendorNames();

        // JComboBox for selecting the category
        kategoriReportAnalisaVendorDropdown = new JComboBox<>(getKategoriNames().toArray(new String[0]));
        kategoriReportAnalisaVendorDropdown.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // Fetch and display vendor analysis data based on the selected category
                    String selectedCategory = (String) kategoriReportAnalisaVendorDropdown.getSelectedItem();
                    fetchAndDisplayVendorAnalysisData(selectedCategory);
                }
            }
        });

        analisaVendorReportTable = new JTable();
        analisaVendorReportTableModel = new DefaultTableModel();
        analisaVendorReportTableModel.addColumn("Nama Vendor");
        analisaVendorReportTableModel.addColumn("Uraian Layanan");
        analisaVendorReportTable.setModel(analisaVendorReportTableModel);

        JScrollPane analisaVendorReportScrollPane = new JScrollPane(analisaVendorReportTable);
        analisaVendorReportScrollPane.setPreferredSize(new Dimension(400, 200));

        // Tambahkan tombol cetak PDF
        Font boldFont = new Font("Arial", Font.BOLD, 10);
        cetakPdfButton = new JButton("Cetak PDF Report Analisa Vendor");
        cetakPdfButton.setBackground(Color.GREEN);
        cetakPdfButton.setForeground(Color.BLACK);
        cetakPdfButton.setOpaque(true);
        cetakPdfButton.setBorderPainted(false);
        cetakPdfButton.setEnabled(true);
        cetakPdfButton.setFont(boldFont);
        cetakPdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCategory = (String) kategoriReportAnalisaVendorDropdown.getSelectedItem();
                JTextField vendorNameField = getVendorNameField();
                JTextField serviceNameField = getServiceNameField();
                handleCetakPdfButton(vendorNameField, serviceNameField, selectedCategory);
            }
        });

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setBackground(Color.YELLOW);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setOpaque(true);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(boldFont);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform the refresh action
                String selectedCategory = (String) kategoriReportAnalisaVendorDropdown.getSelectedItem();
                fetchAndDisplayVendorAnalysisData(selectedCategory);
                JOptionPane.showMessageDialog(null, "Data direfresh!", "Refresh",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(new JLabel("Report Analisa Vendor IT"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("PT Dirgantara Indonesia"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Pilih Kategori:"), gbc);
        gbc.gridx = 2;
        panel.add(kategoriReportAnalisaVendorDropdown, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(refreshButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(analisaVendorReportScrollPane, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(cetakPdfButton, gbc);

        cetakPdfButton.requestFocus();

        return panel;
    }

    private static void handleCetakPdfButton(JTextField vendorNameField, JTextField serviceNameField,
            String selectedCategory) {
        try {
            // Retrieve data from GUI components
            List<AnalisaVendorData> analisaVendorDataList = getAnalisaVendorData(selectedCategory);

            // Check if data is empty before printing PDF
            if (analisaVendorDataList.isEmpty()) {
                // Provide user feedback
                JOptionPane.showMessageDialog(null, "Input data terlebih dahulu sebelum mencetak PDF", "Info",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Print PDF
            createAndPrintPdf(analisaVendorDataList);

            // Provide user feedback
            JOptionPane.showMessageDialog(null, "PDF berhasil dicetak", "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (DocumentException | IOException ex) {
            // Log the exception details for debugging
            ex.printStackTrace();
            // Provide user feedback
            JOptionPane.showMessageDialog(null, "Gagal Mencetak PDF", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static List<String> retrieveCategoriesFromDatabase() {
        List<String> categories = new ArrayList<>();

        try {
            // Establish database connection
            Connection connection = getConnection();

            // Query to retrieve distinct categories from the "analisa_report_vendor" table
            String query = "SELECT DISTINCT kategori FROM analisa";
            try (PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery()) {

                // Process the result set and add categories to the list
                while (resultSet.next()) {
                    String category = resultSet.getString("kategori");
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database exception as needed
        }

        return categories;
    }

    private static List<AnalisaVendorData> getAnalisaVendorData(String selectedCategory) {
        List<AnalisaVendorData> vendorAnalysisDataList = new ArrayList<>();

        // Assuming you have a table named 'vendors' in your database
        String query = "SELECT a.nama_vendor, a.layanan, v.hp, v.kota " +
                "FROM analisa a " +
                "JOIN vendors v ON a.nama_vendor = v.nama_vendor " +
                "WHERE a.kategori = ?";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, selectedCategory);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String vendorName = resultSet.getString("nama_vendor");
                    String serviceName = resultSet.getString("layanan");
                    String vendorPhoneNumber = resultSet.getString("hp");
                    String vendorAddress = resultSet.getString("kota");

                    vendorAnalysisDataList.add(new AnalisaVendorData(vendorName, serviceName, selectedCategory,
                            vendorPhoneNumber, vendorAddress));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return vendorAnalysisDataList;
    }

    private static JTextField getVendorNameField() {
        return vendorNameTextField;
    }

    private static JTextField getServiceNameField() {
        return serviceNameTextField;
    }

    public static void createAndPrintPdf(List<AnalisaVendorData> analisaVendorDataList)
            throws DocumentException, IOException {
        // Get timestamp for the file name
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String timestamp = localDateTime.format(formatter);

        String category = analisaVendorDataList.get(0).getCategory();
        String fileName = "Report_" + category + "_Analisa_Vendor" + ".pdf";

        java.util.Date creationDate = Calendar.getInstance().getTime();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // Add a colorful and artistic header with logo
        PdfPTable headerTable = new PdfPTable(2); // Use 2 columns for header and logo
        headerTable.setWidthPercentage(100);

        // Tambahkan sel untuk logo
        Image logo = Image.getInstance(LOGO_PATH);
        logo.scaleToFit(20, 20);

        PdfPCell logoCell = new PdfPCell(logo, true);
        logoCell.setFixedHeight(40); // Set the fixed height of the cell to limit the image height
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Adjust alignment if necessary
        headerTable.addCell(logoCell);

        // Add title
        PdfPCell titleCell = new PdfPCell();
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24,
                new BaseColor(70, 130, 180)); // Steel Blue
        titleCell.addElement(new Paragraph("Report Analisa Vendor IT", titleFont));
        headerTable.addCell(titleCell);

        // Add headerTable to document
        document.add(headerTable);

        // Add company information and vendor details
        com.itextpdf.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph companyParagraph = new Paragraph("PT Dirgantara Indonesia", infoFont);
        document.add(companyParagraph);

        // Add date of file creation
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = dateFormat.format(creationDate);
        document.add(new Paragraph("Tanggal: " + currentDate, infoFont));

        // Add address
        String companyAddress = "Jalan Pajajaran No. 154 Bandung 4017 West Java - Indonesia";
        document.add(new Paragraph("Alamat Perusahaan: " + companyAddress, infoFont));

        // Add category as a paragraph
        document.add(new Paragraph("Kategori: " + analisaVendorDataList.get(0).getCategory(), infoFont));

        // Add vendor information
        document.add(new Paragraph("Nomor Telepon Vendor: " + analisaVendorDataList.get(0).getVendorPhoneNumber(),
                infoFont));
        document.add(new Paragraph("Alamat Vendor: " + analisaVendorDataList.get(0).getVendorAddress(), infoFont));

        // Add date of file creation
        document.add(new Paragraph("Tanggal Cetak: " + currentDate, infoFont));

        // Create and add table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        PdfPCell vendorHeaderCell = new PdfPCell(new Phrase("Nama Vendor"));
        vendorHeaderCell.setBackgroundColor(BaseColor.BLACK);
        vendorHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        vendorHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        vendorHeaderCell.getPhrase().getFont().setColor(BaseColor.WHITE);
        table.addCell(vendorHeaderCell);

        PdfPCell layananHeaderCell = new PdfPCell(new Phrase("Layanan"));
        layananHeaderCell.setBackgroundColor(BaseColor.BLACK);
        layananHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        layananHeaderCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        layananHeaderCell.getPhrase().getFont().setColor(BaseColor.WHITE);
        table.addCell(layananHeaderCell);

        for (AnalisaVendorData data : analisaVendorDataList) {
            table.addCell(data.getVendorName());
            table.addCell(data.getServiceName());
        }

        // Add table to the document
        document.add(table);

        // Add footer
        Paragraph footer = new Paragraph("Vendor Management IT " + timestamp);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        // Print the file name to the console for debugging
        System.out.println("PDF file created: " + fileName);
    }

    private static class AnalisaVendorData {
        private final String vendorName;
        private final String serviceName;
        private final String category;
        private final String vendorPhoneNumber;
        private final String vendorAddress;

        public AnalisaVendorData(String vendorName, String serviceName, String category, String vendorPhoneNumber,
                String vendorAddress) {
            this.vendorName = vendorName;
            this.serviceName = serviceName;
            this.category = category;
            this.vendorPhoneNumber = vendorPhoneNumber;
            this.vendorAddress = vendorAddress;
        }

        public String getVendorPhoneNumber() {
            return vendorPhoneNumber;
        }

        public String getVendorAddress() {
            return vendorAddress;
        }

        public String getVendorName() {
            return vendorName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getCategory() {
            return category;
        }
    }

    private static void fetchAndDisplayVendorAnalysisData(String selectedCategory) {
        // Fetch vendor analysis data from the database based on the selected category
        List<AnalisaVendorData> vendorAnalysisDataList = getVendorAnalysisDataPDF(selectedCategory);

        // Update the table with the fetched data
        updateVendorAnalysisTable(vendorAnalysisDataList);
    }

    private static List<AnalisaVendorData> getVendorAnalysisDataPDF(String selectedCategory) {
        List<AnalisaVendorData> vendorAnalysisDataList = new ArrayList<>();

        // Assuming you have a table named 'vendor' in your database
        String query = "SELECT a.nama_vendor, a.layanan, v.hp, v.kota " +
                "FROM analisa a " +
                "JOIN vendors v ON a.nama_vendor = v.nama_vendor " +
                "WHERE a.kategori = ?";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, selectedCategory);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String vendorName = resultSet.getString("nama_vendor");
                    String serviceName = resultSet.getString("layanan");
                    String vendorPhoneNumber = resultSet.getString("hp");
                    String vendorAddress = resultSet.getString("kota");

                    vendorAnalysisDataList.add(new AnalisaVendorData(vendorName, serviceName, selectedCategory,
                            vendorPhoneNumber, vendorAddress));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return vendorAnalysisDataList;
    }

    private static void updateVendorAnalysisTable(List<AnalisaVendorData> vendorAnalysisDataList) {
        // Assuming analisaVendorReportTable is a JTable
        DefaultTableModel model = (DefaultTableModel) analisaVendorReportTable.getModel();
        model.setRowCount(0); // Clear existing data

        for (AnalisaVendorData data : vendorAnalysisDataList) {
            model.addRow(new Object[] {
                    data.getVendorName(),
                    data.getServiceName(),
                    data.getVendorPhoneNumber(),
                    data.getVendorAddress()
            });
        }
    }

    private static JPanel CreateTableVendors() {
        JPanel panel = new JPanel(new BorderLayout());

        // Fetch data from the database
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Nama Vendor");
        model.addColumn("HP");
        model.addColumn("Alamat");
        model.addColumn("Status");
        model.addColumn("Layanan");
        model.addColumn("Kategori");

        fetchDataFromDatabase(model);

        JTable vendorTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(vendorTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton editButton = new JButton("Edit Vendor");
        JButton refreshButton = new JButton("Refresh Data");

        // Set the background color to yellow for both buttons
        editButton.setBackground(Color.YELLOW);
        refreshButton.setBackground(Color.YELLOW);

        // Set the UI property to the default look and feel
        editButton.setUI(new BasicButtonUI());
        refreshButton.setUI(new BasicButtonUI());

        // Style the buttons
        editButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(editButton);
        buttonPanel.add(refreshButton);

        // Add some padding to the panel
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add a line border to the panel
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add search functionality
        JTextField searchField = new JTextField(20);
        JComboBox<String> categoryComboBox = new JComboBox<>(getUniqueCategories(model));
        JButton searchButton = new JButton("Search");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by Vendor Name:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryComboBox);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchField.getText().trim();
                String selectedCategory = (String) categoryComboBox.getSelectedItem();

                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                List<RowFilter<Object, Object>> filters = new ArrayList<>();

                // Add filter for vendor name
                filters.add(RowFilter.regexFilter("(?i)" + searchTerm, 0));

                // Add filter for category
                if (!selectedCategory.equals("All")) {
                    filters.add(RowFilter.regexFilter("(?i)" + selectedCategory, 5));
                }

                sorter.setRowFilter(RowFilter.andFilter(filters));
                vendorTable.setRowSorter(sorter);
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = vendorTable.getSelectedRow();
            if (selectedRow != -1) {
                String namaVendor = (String) model.getValueAt(selectedRow, 0);
                String status = (String) model.getValueAt(selectedRow, 3);
                String hp = (String) model.getValueAt(selectedRow, 1);
                String kota = (String) model.getValueAt(selectedRow, 2);
                String layanan = (String) model.getValueAt(selectedRow, 4);
                String kategori = (String) model.getValueAt(selectedRow, 5);

                // Fetch additional information for the selected vendor
                String vendorID = fetchVendorID(namaVendor);

                // Create a panel for the form
                JPanel formPanel = new JPanel(new GridLayout(7, 2));
                formPanel.add(new JLabel("ID Vendor:"));
                formPanel.add(new JLabel(vendorID)); // Display the vendor ID
                formPanel.add(new JLabel("Nama Vendor:"));
                JTextField namaVendorField = new JTextField(namaVendor);
                formPanel.add(namaVendorField);
                formPanel.add(new JLabel("HP:"));
                JTextField hpField = new JTextField(hp);
                formPanel.add(hpField);
                formPanel.add(new JLabel("Kota:"));
                JTextField kotaField = new JTextField(kota);
                formPanel.add(kotaField);
                formPanel.add(new JLabel("Status:"));
                JComboBox<String> statusComboBox = new JComboBox<>(new String[] { "Calon Vendor", "Vendor" });
                statusComboBox.setSelectedItem(status);
                formPanel.add(statusComboBox);
                formPanel.add(new JLabel("Layanan:"));
                JTextField layananField = new JTextField(layanan);
                formPanel.add(layananField);
                formPanel.add(new JLabel("Kategori:"));
                JLabel kategoriLabel = new JLabel(kategori);
                formPanel.add(kategoriLabel);

                int result = JOptionPane.showConfirmDialog(panel, formPanel,
                        "Edit Vendor Information", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    // Update the vendor information if OK is selected
                    String newNamaVendor = namaVendorField.getText();
                    String newHP = hpField.getText();
                    String newKota = kotaField.getText();
                    String newLayanan = layananField.getText();
                    String newStatus = (String) statusComboBox.getSelectedItem();

                    if (!namaVendor.equals(newNamaVendor) || !hp.equals(newHP) || !kota.equals(newKota)
                            || !layanan.equals(newLayanan) || !status.equals(newStatus)) {
                        // Update the information only if there are changes
                        updateVendorInfo(namaVendor, newNamaVendor, newHP, newKota, newStatus);
                        model.setValueAt(newNamaVendor, selectedRow, 0);
                        model.setValueAt(newHP, selectedRow, 1);
                        model.setValueAt(newKota, selectedRow, 2);
                        model.setValueAt(newStatus, selectedRow, 3);
                        model.setValueAt(newLayanan, selectedRow, 4);
                        // Do not update the category as requested
                        JOptionPane.showMessageDialog(panel, "Informasi vendor berhasil diupdate!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Tidak ada perubahan yang dilakukan.", "Info",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Tolong pilih vendor yang akan diedit!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> {
            // Refresh the data from the database
            model.setRowCount(0); // Clear existing data
            fetchDataFromDatabase(model);
            JOptionPane.showMessageDialog(panel, "Data direfresh!", "Refresh",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    private static void updateVendorInfo(String oldNamaVendor, String newNamaVendor, String newHP,
            String newKota, String newStatus) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Fetch additional information for the selected vendor
            String vendorID = fetchVendorID(oldNamaVendor);

            String query = "UPDATE vendors SET nama_vendor = ?, hp = ?, kota = ?, status = ? WHERE nama_vendor = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newNamaVendor);
                statement.setString(2, newHP);
                statement.setString(3, newKota);
                statement.setString(4, newStatus);
                statement.setString(5, oldNamaVendor);
                statement.executeUpdate();
            }

            // Update the vendor status separately
            updateVendorStatusAndID(newNamaVendor, newStatus);

            // Display success message
            JOptionPane.showMessageDialog(null, "Informasi vendor berhasil diupdate!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String fetchVendorID(String namaVendor) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT id FROM vendors WHERE nama_vendor = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, namaVendor);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void fetchDataFromDatabase(DefaultTableModel model) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT v.nama_vendor, v.hp, v.kota, v.status, a.layanan, a.kategori " +
                    "FROM vendors v " +
                    "JOIN analisa a ON v.nama_vendor = a.nama_vendor";
            try (PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ArrayList<String> rowData = new ArrayList<>();
                    rowData.add(resultSet.getString("nama_vendor"));
                    rowData.add(resultSet.getString("hp"));
                    rowData.add(resultSet.getString("kota"));
                    rowData.add(resultSet.getString("status"));
                    rowData.add(resultSet.getString("layanan"));
                    rowData.add(resultSet.getString("kategori"));
                    model.addRow(rowData.toArray());
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String[] getUniqueCategories(DefaultTableModel model) {
        List<String> categories = new ArrayList<>();
        categories.add("All");

        for (int i = 0; i < model.getRowCount(); i++) {
            String category = (String) model.getValueAt(i, 5);
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }

        return categories.toArray(new String[0]);
    }

    private static void updateVendorStatusAndID(String namaVendor, String newStatus) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query;
            String newID = generateNewVendorID(newStatus, connection);

            if ("Calon Vendor".equals(newStatus) || "Vendor".equals(newStatus)) {
                query = "UPDATE vendors SET status = ?, id = ? WHERE nama_vendor = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, newStatus);
                    statement.setString(2, newID);
                    statement.setString(3, namaVendor);
                    statement.executeUpdate();
                }
            } else {
                // Handle other status values if needed
                query = "UPDATE vendors SET status = ? WHERE nama_vendor = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    // Adjust the statusValue based on your actual mapping
                    int statusValue = 2; // Update this based on the actual status mapping
                    statement.setInt(1, statusValue);
                    statement.setString(2, namaVendor);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String generateNewVendorID(String newStatus, Connection connection) {
        String prefix = "";
        if ("Calon Vendor".equals(newStatus)) {
            prefix = "CVIT";
        } else if ("Vendor".equals(newStatus)) {
            prefix = "VIT";
        }

        try {
            String query = "SELECT MAX(CAST(SUBSTRING(id, 4) AS INT)) + 1 AS next_id " +
                    "FROM vendors " +
                    "WHERE id LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, prefix + "%");
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int nextID = resultSet.getInt("next_id");
                        return prefix + String.format("%03d", Math.max(1, nextID));
                        // Use Math.max(1, nextID) to ensure that if nextID is 0, it starts from 1.
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Default to a new ID if an error occurs
        return prefix + "001";
    }

    private static JPanel createAnalisaVendorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Fetch vendors from the database
        List<String> vendorNames = getVendorNames();

        vendorDropdown = new JComboBox<>(vendorNames.toArray(new String[0]));
        vendorDropdown.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedVendor = (String) vendorDropdown.getSelectedItem();
                    fillAlamatData(selectedVendor);
                }
            }
        });

        List<String> kategoriNames = getKategoriNames();
        kategoriDropdown = new JComboBox<>(kategoriNames.toArray(new String[0]));

        alamatLabel = new JLabel();
        alamatLabel.setPreferredSize(new Dimension(200, 50));

        // Set the border of alamatLabel to null
        alamatLabel.setBorder(null);
        Font boldFont = new Font("Arial", Font.BOLD, 10);
        JButton tambahLayananButton = new JButton("Tambah Data Layanan");
        tambahLayananButton.setBackground(Color.GREEN);
        tambahLayananButton.setForeground(Color.BLACK);
        tambahLayananButton.setOpaque(true);
        tambahLayananButton.setBorderPainted(false);
        tambahLayananButton.setFont(boldFont);
        tambahLayananButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTambahLayananForm();
            }
        });

        JButton tambahAnalisaButton = new JButton("Tambah Data Analisa Report");
        tambahAnalisaButton.setBackground(Color.BLUE);
        tambahAnalisaButton.setForeground(Color.WHITE);
        tambahAnalisaButton.setOpaque(true);
        tambahAnalisaButton.setBorderPainted(false);
        tambahAnalisaButton.setFont(boldFont);
        tambahAnalisaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tambahDataAnalisaReport();
            }
        });

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setBackground(Color.YELLOW);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setOpaque(true);
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(boldFont);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform the refresh action, for example:
                fetchAndDisplayLayananData();
                JOptionPane.showMessageDialog(null, "Data direfresh!", "Refresh",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        layananTable = new JTable();
        DefaultTableModel layananTableModel = new DefaultTableModel();
        layananTableModel.addColumn("No");
        layananTableModel.addColumn("Uraian Layanan");
        layananTable.setModel(layananTableModel); // Set the model after creating the table

        fetchAndDisplayLayananData();

        JScrollPane layananScrollPane = new JScrollPane(layananTable);
        layananScrollPane.setPreferredSize(new Dimension(300, 150));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Pilih Vendor:"), gbc);

        gbc.gridx = 1;
        panel.add(vendorDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Alamat Vendor:"), gbc);

        gbc.gridx = 1;
        panel.add(alamatLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Pilih Kategori:"), gbc);

        gbc.gridx = 1;
        panel.add(kategoriDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(tambahLayananButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(layananScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(tambahAnalisaButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(refreshButton, gbc);

        // Fetch and display initial layanan data
        fetchAndDisplayLayananData();

        return panel;
    }

    private static void fetchAndDisplayLayananData() {
        // Fetch vendor names and display in the vendor dropdown
        List<String> vendorNames = getVendorNames();
        DefaultComboBoxModel<String> vendorModel = new DefaultComboBoxModel<>(vendorNames.toArray(new String[0]));
        vendorDropdown.setModel(vendorModel);

        // Fetch kategori names and display in the kategori dropdown
        List<String> kategoriNames = getKategoriNames();
        DefaultComboBoxModel<String> kategoriModel = new DefaultComboBoxModel<>(kategoriNames.toArray(new String[0]));
        kategoriDropdown.setModel(kategoriModel);

        // Fetch layanan data from the database and display it in the table
        List<String> layananData = getLayananData();
        updateLayananTable(layananData);
    }

    private static List<String> getLayananData() {
        List<String> layananData = new ArrayList<>();
        String query = "SELECT uraian FROM layanan";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                layananData.add(resultSet.getString("uraian"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return layananData;
    }

    private static void updateLayananTable(List<String> layananData) {
        DefaultTableModel model = (DefaultTableModel) layananTable.getModel();
        model.setRowCount(0); // Clear existing data

        for (int i = 0; i < layananData.size(); i++) {
            model.addRow(new Object[] { i + 1, layananData.get(i) });
        }
    }

    private static List<String> getVendorNames() {
        List<String> vendorNames = new ArrayList<>();
        String query = "SELECT nama_vendor FROM vendors";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                vendorNames.add(resultSet.getString("nama_vendor"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return vendorNames;
    }

    private static void fillAlamatData(String selectedVendor) {
        String query = "SELECT kota FROM vendors WHERE nama_vendor = ?";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, selectedVendor);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String kota = resultSet.getString("kota");
                    alamatLabel.setText(kota);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static List<String> getKategoriNames() {
        List<String> kategoriNames = new ArrayList<>();
        String query = "SELECT kategori FROM kategori";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                kategoriNames.add(resultSet.getString("kategori"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return kategoriNames;
    }

    private static void showTambahLayananForm() {
        JFrame layananFrame = new JFrame("Tambah Data Layanan");
        layananFrame.setSize(300, 200);
        layananFrame.setLocationRelativeTo(null);

        JPanel layananPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        layananPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Font boldFont = new Font("Arial", Font.BOLD, 10);
        JTextField uraianField = new JTextField(20);
        JButton simpanLayananButton = new JButton("Simpan Layanan");
        simpanLayananButton.setBackground(Color.GREEN);
        simpanLayananButton.setForeground(Color.BLACK);
        simpanLayananButton.setOpaque(true);
        simpanLayananButton.setBorderPainted(false);
        simpanLayananButton.setFont(boldFont);
        simpanLayananButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the entered data
                String uraian = uraianField.getText().trim();

                // Validate the data
                if (uraian.isEmpty()) {
                    JOptionPane.showMessageDialog(layananFrame, "Uraian Layanan harus diisi tidak boleh kosong!",
                            "Peringatan",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Add the layanan to the table
                addLayananToTable(uraian);

                // Clear the input field
                uraianField.setText("");

                // Close the layananFrame
                layananFrame.dispose();
                JOptionPane.showMessageDialog(null, "Data Layanan Berhasil Ditambahkan", "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        layananPanel.add(new JLabel("Uraian Layanan:"), gbc);

        gbc.gridx = 1;
        layananPanel.add(uraianField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0); // Add top margin
        layananPanel.add(simpanLayananButton, gbc);

        layananFrame.getContentPane().add(layananPanel);
        layananFrame.setVisible(true);
    }

    private static void addLayananToTable(String uraian) {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) layananTable.getModel();
            model.addRow(new Object[] { model.getRowCount() + 1, uraian });
            model.fireTableDataChanged(); // Notify the table about the data change

            // Add the new data to the database
            saveLayananToDatabase(uraian);
        });
    }

    private static void saveLayananToDatabase(String uraian) {
        String query = "INSERT INTO layanan (uraian) VALUES (?)";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, uraian);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void tambahDataAnalisaReport() {
        // Ambil data yang diperlukan dari komponen-komponen di tab Analisa Vendor
        String selectedVendor = (String) vendorDropdown.getSelectedItem();
        String selectedKategori = (String) kategoriDropdown.getSelectedItem();
        List<String> selectedLayanan = getSelectedLayanan();

        // Display the updated layanan data in the table
        fetchAndDisplayLayananData();

        if (layananTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null,
                    "Tidak ada data Layanan. Silakan tambahkan data Layanan terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Simpan data ke dalam tabel Analisa di database
        saveAnalisaToDatabase(selectedVendor, selectedKategori, selectedLayanan);

        JOptionPane.showMessageDialog(null, "Data analisa report berhasil disimpan!", "Sukses",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static List<String> getSelectedLayanan() {
        List<String> selectedLayanan = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) layananTable.getModel();

        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Object value = model.getValueAt(i, 1);
            if (value != null) {
                selectedLayanan.add(value.toString());
            }
        }

        return selectedLayanan;
    }

    private static void saveAnalisaToDatabase(String selectedVendor, String selectedKategori,
            List<String> selectedLayanan) {
        // Simpan data ke dalam tabel Analisa di database
        String insertAnalisaQuery = "INSERT INTO analisa (id, nama_vendor, kategori, layanan) VALUES (?, ?, ?, ?)";
        String deleteLayananQuery = "DELETE FROM layanan WHERE uraian = ?";
        String maxIdQuery = "SELECT COALESCE(MAX(id), 0) FROM analisa";

        try (Connection connection = getConnection();
                PreparedStatement insertStatement = connection.prepareStatement(insertAnalisaQuery);
                PreparedStatement deleteStatement = connection.prepareStatement(deleteLayananQuery);
                PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery)) {

            // Get the maximum ID from the analisa table
            int maxId = 0;
            try (ResultSet maxIdResult = maxIdStatement.executeQuery()) {
                if (maxIdResult.next()) {
                    maxId = maxIdResult.getInt(1);
                }
            }

            // Iterate through selected layanan and insert into analisa table
            for (String layanan : selectedLayanan) {
                // Increment the ID for each new entry
                maxId++;

                // Insert into analisa table
                insertStatement.setInt(1, maxId);
                insertStatement.setString(2, selectedVendor);
                insertStatement.setString(3, selectedKategori);
                insertStatement.setString(4, layanan);
                insertStatement.executeUpdate();

                // Delete from layanan table
                deleteStatement.setString(1, layanan);
                deleteStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel createVendorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Vendor
        JTextField namaVendorField = new JTextField(20);
        JTextField hpField = new JTextField(20);
        JTextField kotaField = new JTextField(20);
        JRadioButton calonVendorRadioButton = new JRadioButton("Calon Vendor");
        JRadioButton vendorRadioButton = new JRadioButton("Vendor");

        // Button
        ButtonGroup statusGroup = new ButtonGroup();
        statusGroup.add(calonVendorRadioButton);
        statusGroup.add(vendorRadioButton);

        setButtonProperties(calonVendorRadioButton, vendorRadioButton);

        addComponentsToPanel(panel, namaVendorField, hpField, kotaField, calonVendorRadioButton, vendorRadioButton);

        return panel;
    }

    private static void setButtonProperties(AbstractButton... buttons) {
        Font boldFont = new Font("Arial", Font.BOLD, 12);
        for (AbstractButton button : buttons) {
            button.setFont(boldFont);
            button.setToolTipText("Pilih jika vendor adalah "
                    + (button.getText().equalsIgnoreCase("Calon Vendor") ? "calon vendor" : "vendor"));
        }
    }

    private static void addComponentsToPanel(JPanel panel, JTextField namaVendorField, JTextField hpField,
            JTextField kotaField,
            JRadioButton calonVendorRadioButton, JRadioButton vendorRadioButton) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0); // Add bottom margin

        addLabelAndFieldToPanel(panel, "Nama Vendor:", namaVendorField, gbc, 0, 0);
        addLabelAndFieldToPanel(panel, "Nomor HP:", hpField, gbc, 1, 1);
        addLabelAndFieldToPanel(panel, "Alamat:", kotaField, gbc, 2, 2);

        JPanel radioPanel = createRadioPanel(calonVendorRadioButton, vendorRadioButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(radioPanel, gbc);

        JButton clearButton = createClearButton(namaVendorField, hpField, kotaField, calonVendorRadioButton,
                vendorRadioButton);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(clearButton, gbc);

        JButton simpanButton = createSimpanButton(namaVendorField, hpField, kotaField, calonVendorRadioButton);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(simpanButton, gbc);
    }

    private static void addLabelAndFieldToPanel(JPanel panel, String label, JTextField textField,
            GridBagConstraints gbc, int labelRow, int fieldRow) {
        gbc.gridx = 0;
        gbc.gridy = labelRow;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = fieldRow;
        panel.add(textField, gbc);
    }

    private static JPanel createRadioPanel(JRadioButton calonVendorRadioButton, JRadioButton vendorRadioButton) {
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(new JLabel("Status:"));
        radioPanel.add(calonVendorRadioButton);
        radioPanel.add(vendorRadioButton);
        return radioPanel;
    }

    private static JButton createClearButton(JTextField namaVendorField, JTextField hpField, JTextField kotaField,
            JRadioButton calonVendorRadioButton, JRadioButton vendorRadioButton) {
        JButton clearButton = new JButton(new ImageIcon("bin.png"));
        clearButton.setBackground(Color.RED);
        clearButton.setForeground(Color.BLACK);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);
        clearButton.addActionListener(e -> handleClearButton(namaVendorField, hpField, kotaField,
                calonVendorRadioButton, vendorRadioButton));
        return clearButton;
    }

    private static JButton createSimpanButton(JTextField namaVendorField, JTextField hpField, JTextField kotaField,
            JRadioButton calonVendorRadioButton) {
        JButton simpanButton = new JButton("Simpan Vendor");
        simpanButton.setBackground(Color.GREEN);
        simpanButton.setForeground(Color.BLACK);
        simpanButton.setOpaque(true);
        simpanButton.setBorderPainted(false);
        simpanButton.addActionListener(
                e -> handleSimpanButton(namaVendorField, hpField, kotaField, calonVendorRadioButton));
        return simpanButton;
    }

    private static void handleSimpanButton(JTextField namaVendorField, JTextField hpField, JTextField kotaField,
            JRadioButton calonVendorRadioButton) {
        // Validation
        String namaVendor = namaVendorField.getText().trim();
        String nomorHP = hpField.getText().trim();
        String kota = kotaField.getText().trim();

        if (namaVendor.isEmpty() || nomorHP.isEmpty() || kota.isEmpty()) {
            showWarningMessage("Semua kolom harus diisi.");
            return;
        }

        // Validasi nomor telepon harus berupa angka
        try {
            Long.parseLong(nomorHP);
        } catch (NumberFormatException ex) {
            showWarningMessage("Nomor HP harus berupa angka.");
            return;
        }

        // Data processing
        Vendor vendor = new Vendor();
        vendor.setNamaVendor(namaVendor);
        vendor.setHp(nomorHP);
        vendor.setKota(kota);
        vendor.simpanDataVendor(calonVendorRadioButton.isSelected());

        // Show success message
        showSuccessMessage("Data Vendor berhasil disimpan!");
    }

    private static void handleClearButton(JTextField namaVendorField, JTextField hpField, JTextField kotaField,
            JRadioButton calonVendorRadioButton, JRadioButton vendorRadioButton) {
        namaVendorField.setText("");
        hpField.setText("");
        kotaField.setText("");
        calonVendorRadioButton.setSelected(false);
        vendorRadioButton.setSelected(false);
    }

    private static void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private static void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    private static JPanel createKategoriPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField kategoriTextField = new JTextField(20);
        JTextArea keteranganTextArea = new JTextArea(5, 20);
        Font boldFont = new Font("Arial", Font.BOLD, 12);

        JButton simpanKategoriButton = new JButton("Simpan Kategori");
        simpanKategoriButton.setBackground(Color.GREEN);
        simpanKategoriButton.setForeground(Color.BLACK);
        simpanKategoriButton.setOpaque(true);
        simpanKategoriButton.setBorderPainted(false);
        simpanKategoriButton.setFont(boldFont);
        simpanKategoriButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedKategori = kategoriTextField.getText();
                String keterangan = keteranganTextArea.getText();

                if (selectedKategori.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Masukkan kategori terlebih dahulu!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (keterangan.isEmpty()) {
                    int result = JOptionPane.showConfirmDialog(null, "Keterangan kosong. Lanjutkan menyimpan data?",
                            "Konfirmasi", JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                simpanDataKategori(selectedKategori, keterangan);

                JOptionPane.showMessageDialog(null, "Data kategori berhasil disimpan!", "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton clearButton = new JButton(new ImageIcon("bin.png"));
        clearButton.setBackground(Color.RED);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);
        clearButton.setFont(boldFont);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kategoriTextField.setText("");
                keteranganTextArea.setText("");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Kategori:", 0), gbc);

        gbc.gridx = 1;
        panel.add(kategoriTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0); // Add vertical space
        panel.add(new JLabel("Keterangan:", 0), gbc);

        gbc.gridx = 1;
        panel.add(new JScrollPane(keteranganTextArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0); // Reset insets for the buttons
        panel.add(simpanKategoriButton, gbc);

        gbc.gridy = 3;
        panel.add(clearButton, gbc);

        return panel;
    }

    private static void simpanDataKategori(String kategori, String keterangan) {
        String query = "INSERT INTO kategori (kategori, keterangan) VALUES (?, ?)";

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kategori);
            preparedStatement.setString(2, keterangan);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}

class Vendor {
    private int id;
    private String namaVendor;
    private String hp;
    private String kota;
    private String status;

    public void setNamaVendor(String namaVendor) {
        this.namaVendor = namaVendor;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }

    public void simpanDataVendor(boolean isCalonVendor) {
        int nextId = getNextId(isCalonVendor);
        String statusPrefix = isCalonVendor ? "CVIT" : "VIT";
        String formattedId = String.format("%s%03d", statusPrefix, nextId);

        this.status = isCalonVendor ? "Calon Vendor" : "Vendor";

        String query = "INSERT INTO vendors (id, nama_vendor, hp, kota, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = VendorManagementGUI.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, formattedId);
            preparedStatement.setString(2, namaVendor);
            preparedStatement.setString(3, hp);
            preparedStatement.setString(4, kota);
            preparedStatement.setString(5, status);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private int getNextId(boolean isCalonVendor) {
        String statusPrefix = isCalonVendor ? "CVIT" : "VIT";
        String query = "SELECT MAX(CAST(SUBSTRING(id, LENGTH(?)+1) AS INTEGER)) FROM vendors WHERE id LIKE ?";
        try (Connection connection = VendorManagementGUI.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, statusPrefix);
            preparedStatement.setString(2, statusPrefix + "%");

            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1;
    }
}
