package se.sundsvall.ai.flow.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import se.sundsvall.ai.flow.model.flowdefinition.exception.FlowException;

class DocumentUtilTest {

	private static final String SAMPLE_PDF_WITH_IMAGE = "samples/sample-with-image.pdf";

	@Test
	void testIsPdf() throws IOException {
		// create a minimal PDF in memory
		byte[] pdfBytes;
		try (var document = new PDDocument()) {
			document.addPage(new PDPage());
			try (var outputStream = new ByteArrayOutputStream()) {
				document.save(outputStream);
				pdfBytes = outputStream.toByteArray();
			}
		}

		assertThat(DocumentUtil.isPdf(pdfBytes)).isTrue();
		// extraction path should not throw
		DocumentUtil.extractTextFromPdf(pdfBytes);
	}

	@Test
	void testIsNotPdf() {
		var notPdf = "not a pdf".getBytes();
		assertThat(DocumentUtil.isPdf(notPdf)).isFalse();
		assertThatThrownBy(() -> DocumentUtil.extractTextFromPdf(notPdf))
			.isInstanceOf(FlowException.class)
			.hasMessageContaining("PDF");
	}

	@Test
	void TestIsDocx() throws Exception {
		byte[] docxBytes;
		try (var docx = new XWPFDocument()) {
			try (var outputStream = new ByteArrayOutputStream()) {
				docx.write(outputStream);
				docxBytes = outputStream.toByteArray();
			}
		}

		assertThat(DocumentUtil.isDocx(docxBytes)).isTrue();
		DocumentUtil.extractTextFromDocx(docxBytes);
	}

	@Test
	void testIsNotDocx() {
		var notDocx = new byte[] {
			0x01, 0x02, 0x03
		};
		assertThat(DocumentUtil.isDocx(notDocx)).isFalse();
		assertThatThrownBy(() -> DocumentUtil.extractTextFromDocx(notDocx))
			.isInstanceOf(FlowException.class)
			.hasMessageContaining("DOCX");
	}

	@Test
	void testRemoveImages_shouldRemoveImagesFromPdf() throws Exception {
		// Load sample PDF with image from test resources
		final var originalPdfBytes = new ClassPathResource(SAMPLE_PDF_WITH_IMAGE).getContentAsByteArray();

		// Count images in original
		final var originalImageCount = countImagesInPdf(originalPdfBytes);
		assertThat(originalImageCount).as("Sample PDF should contain at least one image").isGreaterThan(0);

		// Remove images
		final var multipartFile = new MockMultipartFile(
			"file", "test.pdf", "application/pdf", originalPdfBytes);
		final var resultBytes = DocumentUtil.removeImages(multipartFile);

		// Verify result is still a valid PDF
		assertThat(DocumentUtil.isPdf(resultBytes)).isTrue();

		// Verify images are removed
		final var resultImageCount = countImagesInPdf(resultBytes);
		assertThat(resultImageCount).isZero();

		// Verify file size is smaller
		assertThat(resultBytes).hasSizeLessThan(originalPdfBytes.length);
	}

	@Test
	void testRemoveImages_shouldReturnOriginalWhenUnsupportedType() {
		// Create a byte array that's neither PDF nor DOCX
		final var originalBytes = "This is just plain text, not a PDF or DOCX".getBytes();

		final var multipartFile = new MockMultipartFile(
			"file", "test.txt", "text/plain", originalBytes);
		final var resultBytes = DocumentUtil.removeImages(multipartFile);

		// Verify returns exact same bytes
		assertThat(resultBytes).isEqualTo(originalBytes);
	}

	@Test
	void testRemoveImagesWithCorruptPdf_shouldReturnOriginal() {
		// Create corrupted PDF data that won't pass isPdf() check
		final var corruptedPdf = "PDF-1.4\n%corrupted data".getBytes();

		final var multipartFile = new MockMultipartFile(
			"file", "corrupted.pdf", "application/pdf", corruptedPdf);

		// Corrupted data that doesn't parse as PDF should return original bytes
		final var resultBytes = DocumentUtil.removeImages(multipartFile);
		assertThat(resultBytes).isEqualTo(corruptedPdf);
	}

	@Test
	void testRemoveImagesWithCorruptDocx_shouldReturnOriginal() {
		// Create corrupted DOCX data that won't pass isDocx() check
		final var corruptedDocx = new byte[] {
			0x50, 0x4B, 0x03, 0x04, // ZIP signature
			0x00, 0x00              // corrupted rest
		};

		final var multipartFile = new MockMultipartFile(
			"file", "corrupted.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			corruptedDocx);

		// Corrupted data that doesn't parse as DOCX should return original bytes
		final var resultBytes = DocumentUtil.removeImages(multipartFile);
		assertThat(resultBytes).isEqualTo(corruptedDocx);
	}

	private int countImagesInPdf(final byte[] pdfBytes) throws IOException {
		try (final var document = Loader.loadPDF(pdfBytes)) {
			var imageCount = 0;
			for (final var page : document.getPages()) {
				final var resources = page.getResources();
				if (resources != null) {
					for (final var name : resources.getXObjectNames()) {
						if (resources.getXObject(name) instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
							imageCount++;
						}
					}
				}
			}
			return imageCount;
		}
	}
}
