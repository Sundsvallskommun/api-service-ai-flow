package se.sundsvall.ai.flow.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.ai.flow.model.flowdefinition.exception.FlowException;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public final class DocumentUtil {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentUtil.class);

	private DocumentUtil() {}

	public static boolean isPdf(final byte[] data) {
		try {
			extractTextFromPdf(data);

			return true;
		} catch (final Exception _) {
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
		} catch (final Exception _) {
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

	public static byte[] removeImages(final MultipartFile inputMultipartFile) {
		try {
			final var data = inputMultipartFile.getBytes();
			LOG.info("Removing images from document of size {} bytes", data.length);

			// Detect file type and route to appropriate handler
			if (isPdf(data)) {
				return removeImagesFromPdf(data);
			}

			// Return original bytes for unsupported formats
			LOG.warn("Unable to remove images from document of size {} bytes", data.length);
			return data;
		} catch (final Exception e) {
			throw new FlowException("Unable to remove images from document", e);
		}
	}

	private static byte[] removeImagesFromPdf(final byte[] data) {
		try (final var document = Loader.loadPDF(data)) {
			// Remove image XObjects from each page's resources
			for (final var page : document.getPages()) {
				removeImagesFromPageResources(page.getResources());
			}

			// Save with compression
			try (final var outputStream = new ByteArrayOutputStream()) {
				document.save(outputStream, CompressParameters.DEFAULT_COMPRESSION);
				final var result = outputStream.toByteArray();
				LOG.info("PDF size after removing images: {} bytes (was {} bytes)", result.length, data.length);
				return result;
			}
		} catch (final Exception e) {
			throw new FlowException("Unable to remove images from PDF document", e);
		}
	}

	private static void removeImagesFromPageResources(final PDResources resources) throws IOException {
		if (resources == null) {
			return;
		}

		final var xObjectDict = resources.getCOSObject().getDictionaryObject(COSName.XOBJECT);
		if (!(xObjectDict instanceof COSDictionary dict)) {
			return;
		}

		// Save all image names to remove
		final var imagesToRemove = new ArrayList<COSName>();
		for (final var name : resources.getXObjectNames()) {
			if (resources.getXObject(name) instanceof PDImageXObject) {
				imagesToRemove.add(name);
			}
		}

		// Remove all collected images
		imagesToRemove.forEach(dict::removeItem);
	}
}
