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

	public ReportGeneratorApp() {
		super("Report Generator");

		initComponents();
		setLayout(null);
		setSize(300, 150);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		generateButton = new JButton("Generate Report");
		generateButton.setBounds(50, 30, 200, 50);
		generateButton.addActionListener(e -> generateReport());

		add(generateButton);
	}

	private Connection createConnection() throws SQLException {
		String url = "jdbc:mysql://localhost:3306/biblioteka";
		String user = "root";
		String password = "rootroot";
		return DriverManager.getConnection(url, user, password);
	}

	private void generateReport() {
		Connection connection = null;

		try {
			// Establish database connection
			connection = createConnection();

			// Load the report design
			JasperReport jasperReport = JasperCompileManager.compileReport("/Users/mefunio/Documents/template/Raport_1.jrxml");

			// Set parameters
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Imie", "John");
			parameters.put("Nazwisko", "Doe");
			parameters.put("Opis", "Sample Report");

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
