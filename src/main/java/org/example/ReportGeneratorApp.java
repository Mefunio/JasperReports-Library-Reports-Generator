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

	private static final String DB_URL = "jdbc:mysql://localhost:3306/biblioteka";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "rootroot";
	private static final String REPORT_BOOKS_AVAILABILITY = "/Users/mefunio/Documents/template/Raport_2.jrxml";

	private JTextField nameField;
	private JTextField lastNameField;
	private JTextField descriptionField;

	private static final Logger LOGGER = Logger.getLogger(ReportGeneratorApp.class.getName());

	static {
		try {
			FileHandler fileHandler = new FileHandler("application.log", true);
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
		setSize(400, 250);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		JLabel imieLabel = new JLabel("Name:");
		imieLabel.setBounds(20, 30, 100, 30);

		nameField = new JTextField();
		nameField.setBounds(120, 30, 200, 30);

		JLabel nazwiskoLabel = new JLabel("Last name:");
		nazwiskoLabel.setBounds(20, 70, 100, 30);

		lastNameField = new JTextField();
		lastNameField.setBounds(120, 70, 200, 30);

		JLabel opisLabel = new JLabel("Description:");
		opisLabel.setBounds(20, 110, 100, 30);

		descriptionField = new JTextField();
		descriptionField.setBounds(120, 110, 200, 30);

		JButton generateButton = new JButton("Generate Report");
		generateButton.setBounds(120, 150, 200, 30);
		generateButton.addActionListener(e -> generateReport());

		add(imieLabel);
		add(nameField);
		add(nazwiskoLabel);
		add(lastNameField);
		add(opisLabel);
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

		Connection connection = null;

		try {
			connection = createConnection();

			JasperReport jasperReport = JasperCompileManager.compileReport(REPORT_BOOKS_AVAILABILITY);

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Imie", nameField.getText());
			parameters.put("Nazwisko", lastNameField.getText());
			parameters.put("Opis", descriptionField.getText());

			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

			JRPdfExporter exporter = getExporter(jasperPrint);
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

		private static JRPdfExporter getExporter(JasperPrint jasperPrint) {
		SimplePdfExporterConfiguration exporterConfig = new SimplePdfExporterConfiguration();
		exporterConfig.setMetadataAuthor("Public Library");
		exporterConfig.setEncrypted(true);

		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setConfiguration(exporterConfig);

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(new File("report.pdf")));
		return exporter;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(ReportGeneratorApp::new);
	}
}
