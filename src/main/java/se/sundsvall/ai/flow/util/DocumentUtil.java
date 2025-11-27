package se.sundsvall.ai.flow.util;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import se.sundsvall.ai.flow.model.flowdefinition.exception.FlowException;

public final class DocumentUtil {

	private DocumentUtil() {}

	public static boolean isPdf(final byte[] data) {
		try {
			extractTextFromPdf(data);

			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	public static void extractTextFromPdf(final byte[] data) {
		try (final var document = Loader.loadPDF(data)) {
			final var text = new PDFTextStripper().getText(document);

			removeBlankLines(text);
		} catch (final Exception e) {
			throw new FlowException("Unable to extract text from PDF document", e);
		}
	}

	public static boolean isDocx(final byte[] data) {
		try {
			extractTextFromDocx(data);

			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	public static void extractTextFromDocx(final byte[] data) {
		try (final var in = new ByteArrayInputStream(data);
			final var file = new XWPFDocument(OPCPackage.open(in))) {
			try (final var extractor = new XWPFWordExtractor(file)) {
				extractor.getText();
			}
		} catch (final Exception e) {
			throw new FlowException("Unable to extract text from DOCX document", e);
		}
	}

	private static String removeBlankLines(final String s) {
		return Arrays.stream(s.split("\n"))
			.map(String::trim)
			.filter(not(String::isBlank))
			.collect(joining("\n"));
	}
}
