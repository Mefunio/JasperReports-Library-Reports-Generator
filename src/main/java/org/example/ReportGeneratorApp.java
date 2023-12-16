package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

public class ReportGeneratorApp extends JFrame {

	private static final String LOG_FILE_NAME = "application.log";
	private static final String DB_URL = "jdbc:mysql://localhost:3306/biblioteka";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "rootroot";
	private static final String REPORT_AVAILABILITY = "/Users/mefunio/Documents/template/Raport_1.jrxml";
	private static final String REPORT_PUBLISHERS = "/Users/mefunio/Documents/template/Raport_2.jrxml";
	private static final String REPORT_REVIEWS = "/Users/mefunio/Documents/template/Raport_3.jrxml";
	private static final String REPORT_LANGUAGE = "/Users/mefunio/Documents/template/Raport_4.jrxml";

	private JTextField nameField;
	private JTextField lastNameField;
	private JTextField descriptionField;
	private JComboBox<String> reportSelector;

	private static final Logger LOGGER = Logger.getLogger(ReportGeneratorApp.class.getName());

	static {
		try {
			FileHandler fileHandler = new FileHandler(LOG_FILE_NAME, true);
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
			LOGGER.setLevel(Level.ALL);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error configuring logging", e);
		}
	}

	public ReportGeneratorApp() {
		super("Report Generator");

		initComponents();
		setLayout(new BorderLayout());
		setSize(500, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		JLabel selectionLabel = new JLabel("Template:");
		selectionLabel.setBounds(20, 30, 100, 30);

		String[] reportOptions = {"Books Availability Report", "Publishers Book Report", "Reviews-Rating Report", "Book Count by Language Report"};
		reportSelector = new JComboBox<>(reportOptions);
		reportSelector.setBounds(100, 30, 300, 30);

		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setBounds(20, 70, 100, 30);

		nameField = new JTextField();
		nameField.setBounds(100, 70, 300, 30);

		JLabel lastNameLabel = new JLabel("Last name:");
		lastNameLabel.setBounds(20, 110, 100, 30);

		lastNameField = new JTextField();
		lastNameField.setBounds(100, 110, 300, 30);

		JLabel descriptionLabel = new JLabel("Description:");
		descriptionLabel.setBounds(20, 150, 100, 30);

		descriptionField = new JTextField();
		descriptionField.setBounds(100, 150, 300, 30);

		JButton generateButton = new JButton("Generate Report");
		generateButton.setBounds(100, 210, 300, 30);
		generateButton.addActionListener(e -> generateReport());

		add(selectionLabel);
		add(reportSelector);
		add(nameLabel);
		add(nameField);
		add(lastNameLabel);
		add(lastNameField);
		add(descriptionLabel);
		add(descriptionField);
		add(generateButton);
	}

	private Connection createConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	}

	private void generateReport() {
		if (nameField.getText().isEmpty() || lastNameField.getText().isEmpty() || descriptionField.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please fill in all fields before generating the report.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Export");
		int userSelection = fileChooser.showSaveDialog(this);

		if (userSelection != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File fileToSave = fileChooser.getSelectedFile();

		if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
			JOptionPane.showMessageDialog(this, "Please enter a file name ending with '.pdf'", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Connection connection = null;

		try {
			connection = createConnection();

			String selectedReport = (String) reportSelector.getSelectedItem();
			assert selectedReport != null;
			String reportPath = getReportPath(selectedReport);

			JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Imie", nameField.getText());
			parameters.put("Nazwisko", lastNameField.getText());
			parameters.put("Opis", descriptionField.getText());

			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

			JRPdfExporter exporter = getExporter(jasperPrint, fileToSave);
			exporter.exportReport();

			JOptionPane.showMessageDialog(this, "Report generated successfully!");

		} catch (SQLException | JRException ex) {
			LOGGER.log(Level.SEVERE, "Error generating report", ex);

			JOptionPane.showMessageDialog(this, "Error generating report. Please check logs for details.", "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					LOGGER.info("Database connection closed successfully.");
				}
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "Error closing database connection", e);
			}
		}
	}

	private static JRPdfExporter getExporter(JasperPrint jasperPrint, File fileToSave) {
		SimplePdfExporterConfiguration exporterConfig = new SimplePdfExporterConfiguration();
		exporterConfig.setMetadataAuthor("Public Library");
		exporterConfig.setEncrypted(false);

		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setConfiguration(exporterConfig);
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileToSave));
		return exporter;
	}

	private String getReportPath(String selectedReport) {
		return switch (selectedReport) {
			case "Books Availability Report" -> REPORT_AVAILABILITY;
			case "Publishers Book Report" -> REPORT_PUBLISHERS;
			case "Reviews-Rating Report" -> REPORT_REVIEWS;
			case "Book Count by Language Report" -> REPORT_LANGUAGE;
			default -> throw new IllegalArgumentException("Invalid report selection");
		};
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(ReportGeneratorApp::new);
	}
}
