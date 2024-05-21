package se.sundsvall.ai.flow.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import se.sundsvall.ai.flow.service.flow.exception.FlowException;

public final class DocumentUtil {

    private DocumentUtil() { }

    public static boolean isPdf(final byte[] data) {
        try {
            extractTextFromPdf(data);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractTextFromPdf(final byte[] data) {
        try (var document = Loader.loadPDF(data)) {
            return new PDFTextStripper().getText(document);
        } catch (Exception e) {
            throw new FlowException("Unable to extract text from PDF document", e);
        }
    }

    public static boolean isDocx(final byte[] data) {
        try {
            extractTextFromDocx(data);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractTextFromDocx(final byte[] data) {
        try (var in = new ByteArrayInputStream(data);
             var file = new XWPFDocument(OPCPackage.open(in))) {
            return new XWPFWordExtractor(file).getText();
        } catch (IOException|InvalidFormatException e) {
            throw new FlowException("Unable to extract text from DOCX document", e);
        }
    }
}
