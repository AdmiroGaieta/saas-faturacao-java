package ao.saas.faturacao.modules.reports.service;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Slf4j
@Service
public class JasperReportService {

    /**
     * Gera um relatório em PDF a partir de um template .jrxml ou .jasper
     * na pasta resources/reports/templates/
     *
     * @param templateName  nome do ficheiro (sem extensão)
     * @param parameters    parâmetros do relatório
     * @param dataSource    lista de dados (pode ser vazia para templates com SQL)
     * @return bytes do PDF gerado
     */
    public byte[] generatePdf(String templateName,
                               Map<String, Object> parameters,
                               Collection<?> dataSource) {
        try {
            JasperPrint print = fillReport(templateName, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException e) {
            log.error("Erro ao gerar PDF '{}': {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar relatório PDF: " + templateName, e);
        }
    }

    /**
     * Gera relatório em XLSX (Excel)
     */
    public byte[] generateXlsx(String templateName,
                                Map<String, Object> parameters,
                                Collection<?> dataSource) {
        try {
            JasperPrint print = fillReport(templateName, parameters, dataSource);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setOnePagePerSheet(false);
            config.setDetectCellType(true);
            config.setCollapseRowSpan(false);
            exporter.setConfiguration(config);
            exporter.exportReport();

            return out.toByteArray();
        } catch (JRException e) {
            log.error("Erro ao gerar XLSX '{}': {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar relatório XLSX: " + templateName, e);
        }
    }

    /**
     * Gera relatório em CSV
     */
    public byte[] generateCsv(String templateName,
                               Map<String, Object> parameters,
                               Collection<?> dataSource) {
        try {
            JasperPrint print = fillReport(templateName, parameters, dataSource);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JRCsvExporter exporter = new JRCsvExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(out, "UTF-8"));
            exporter.exportReport();

            return out.toByteArray();
        } catch (JRException e) {
            throw new RuntimeException("Erro ao gerar CSV: " + templateName, e);
        }
    }

    // ── Helper interno ─────────────────────────────────────────────

    private JasperPrint fillReport(String templateName,
                                   Map<String, Object> parameters,
                                   Collection<?> dataSource) throws JRException {
        JasperReport compiled = loadTemplate(templateName);
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(
                dataSource != null ? dataSource : Collections.emptyList());
        if (parameters == null) parameters = new HashMap<>();
        return JasperFillManager.fillReport(compiled, parameters, ds);
    }

    /**
     * Carrega e compila template (.jrxml → .jasper em cache)
     */
    private JasperReport loadTemplate(String templateName) throws JRException {
        // Tenta primeiro .jasper compilado (mais rápido)
        String jasperPath = "reports/templates/" + templateName + ".jasper";
        String jrxmlPath  = "reports/templates/" + templateName + ".jrxml";

        try {
            ClassPathResource jasperRes = new ClassPathResource(jasperPath);
            if (jasperRes.exists()) {
                try (InputStream is = jasperRes.getInputStream()) {
                    return (JasperReport) JRLoader.loadObject(is);
                }
            }
        } catch (IOException e) {
            log.debug("Ficheiro .jasper não encontrado, a compilar .jrxml");
        }

        // Compila .jrxml em memória
        try {
            ClassPathResource jrxmlRes = new ClassPathResource(jrxmlPath);
            try (InputStream is = jrxmlRes.getInputStream()) {
                return JasperCompileManager.compileReport(is);
            }
        } catch (IOException e) {
            throw new JRException("Template não encontrado: " + templateName, e);
        }
    }
}
