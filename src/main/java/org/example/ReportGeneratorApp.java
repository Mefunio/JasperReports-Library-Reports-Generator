package org.example;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;

public class ReportGeneratorApp extends JFrame {

	private JButton generateButton;
	private JTextField imieField;
	private JTextField nazwiskoField;
	private JTextField opisField;

	public ReportGeneratorApp() {
		super("Report Generator");

		initComponents();
		setLayout(null);
		setSize(400, 250);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		JLabel imieLabel = new JLabel("Name:");
		imieLabel.setBounds(20, 30, 100, 30);

		imieField = new JTextField();
		imieField.setBounds(120, 30, 200, 30);

		JLabel nazwiskoLabel = new JLabel("Last name:");
		nazwiskoLabel.setBounds(20, 70, 100, 30);

		nazwiskoField = new JTextField();
		nazwiskoField.setBounds(120, 70, 200, 30);

		JLabel opisLabel = new JLabel("Description:");
		opisLabel.setBounds(20, 110, 100, 30);

		opisField = new JTextField();
		opisField.setBounds(120, 110, 200, 30);

		generateButton = new JButton("Generate Report");
		generateButton.setBounds(120, 150, 200, 30);
		generateButton.addActionListener(e -> generateReport());

		add(imieLabel);
		add(imieField);
		add(nazwiskoLabel);
		add(nazwiskoField);
		add(opisLabel);
		add(opisField);
		add(generateButton);
	}


	private Connection createConnection() throws SQLException {
		String url = "jdbc:mysql://localhost:3306/biblioteka";
		String user = "root";
		String password = "rootroot";
		return DriverManager.getConnection(url, user, password);
	}

	private void generateReport() {
		// Check if fields are filled
		if (imieField.getText().isEmpty() || nazwiskoField.getText().isEmpty() || opisField.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please fill in all fields before generating the report.", "Error", JOptionPane.ERROR_MESSAGE);
			return; // Exit the method if fields are not filled
		}

		Connection connection = null;

		try {
			// Establish database connection
			connection = createConnection();

			// Load the report design
			JasperReport jasperReport = JasperCompileManager.compileReport("/Users/mefunio/Documents/template/Raport_1.jrxml");

			// Set parameters from text fields
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Imie", imieField.getText());
			parameters.put("Nazwisko", nazwiskoField.getText());
			parameters.put("Opis", opisField.getText());

			// Create a JasperPrint object
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

			// Export to PDF
			JRPdfExporter exporter = new JRPdfExporter();
			exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRPdfExporterParameter.OUTPUT_FILE, new File("report.pdf"));
			exporter.exportReport();

			JOptionPane.showMessageDialog(this, "Report generated successfully!");

		} catch (SQLException | JRException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			// Close the database connection
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(ReportGeneratorApp::new);
	}
}
