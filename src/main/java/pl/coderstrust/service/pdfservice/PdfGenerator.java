package pl.coderstrust.service.pdfservice;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.coderstrust.model.Company;
import pl.coderstrust.model.Invoice;
import pl.coderstrust.model.InvoiceEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class PdfGenerator {

  private final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);
  private Document document;
  private PdfFontsProvider fonts = new PdfFontsProvider();
  private DateFormat dateFormat;
  private ByteArrayOutputStream generatedPdf = new ByteArrayOutputStream();

  public PdfGenerator() {
    document = new Document();

    document.setMargins(
        Configuration.DEFAULT_MARGIN_SIZE,
        Configuration.DEFAULT_MARGIN_SIZE,
        Configuration.DEFAULT_MARGIN_SIZE,
        Configuration.DEFAULT_MARGIN_SIZE
    );

    try {
      PdfWriter.getInstance(document, generatedPdf);
    } catch (DocumentException ex) {
      logger.warn(
          " from PdfGenerator in PdfGenerator " + ExceptionMessage.PDF_INSTANTIATION_INTERRPUT, ex);
      throw new PdfServiceException(ExceptionMessage.PDF_INSTANTIATION_INTERRPUT, ex);
    }
    dateFormat = new SimpleDateFormat(Configuration.DATE_FORMAT);
    document.open();
  }

  public ByteArrayInputStream invoiceToPdf(Invoice invoice) {
    try {
      document.add(getInvoiceHeaderParagraph(invoice.getName()));

      document.add(Chunk.NEWLINE);
      document.add(getPropertyValueParagraph("ID", Long.toString(invoice.getId())));
      document.add(getPropertyValueParagraph("Issue Date", invoice.getIssueDate().toString()));
      document.add(getPropertyValueParagraph("Payment Date", invoice.getPaymentDate().toString()));
      document
          .add(getPropertyValueParagraph("Payment State", invoice.getPaymentState().toString()));
      document.add(getCompanyTable(invoice.getSeller(), invoice.getBuyer()));
      document.add(getProductsTable(invoice.getProducts()));
      document.add(Chunk.NEWLINE);
      document
          .add(getPropertyValueParagraph("Invoice generated at", dateFormat.format(new Date())));
    } catch (DocumentException ex) {
      logger.warn(
          " from invoiceToPdf in PdfGenerator " + ExceptionMessage.PDF_FILL_INTERRPUT, ex);
      throw new PdfServiceException(ExceptionMessage.PDF_FILL_INTERRPUT, ex);
    }
    document.close();

    return new ByteArrayInputStream(generatedPdf.toByteArray());
  }

  private Paragraph getInvoiceHeaderParagraph(String invoiceName) {
    Paragraph paragraph = new Paragraph();
    paragraph.setAlignment(Element.ALIGN_CENTER);
    paragraph.add(new Chunk("Invoice: ", fonts.getHeaderPropertyFont()));
    paragraph.add(new Chunk(invoiceName, fonts.getHeaderValueFont()));
    return paragraph;
  }

  private Paragraph getPropertyValueParagraph(String property, String value) {
    Paragraph paragraph = new Paragraph();
    paragraph.setAlignment(Element.ALIGN_LEFT);
    paragraph.add(new Chunk(property + ": ", fonts.getPropertyFont()));
    paragraph.add(new Chunk(value, fonts.getValueFont()));
    return paragraph;
  }


  private PdfPTable getCompanyTable(Company seller, Company buyer) {
    PdfPTable table = new PdfPTable(Configuration.COMPANIES_TABLE_COLUMNS_COUNT);
    table.setSpacingBefore(Configuration.TABLE_SPACING);
    table.setSpacingAfter(Configuration.TABLE_SPACING);

    addTableHeader(table, Stream.of("Property", "Seller", "Buyer"));

    addRowsCompanyTable(table, "Company name", seller.getName(), buyer.getName());
    addRowsCompanyTable(table, "Company id", Long.toString(seller.getId()),
        Long.toString(buyer.getId()));
    addRowsCompanyTable(table, "Company address", seller.getAddress(), buyer.getAddress());
    addRowsCompanyTable(table, "Company city", seller.getCity(), buyer.getCity());
    addRowsCompanyTable(table, "Company zip code", seller.getZipCode(), buyer.getZipCode());
    addRowsCompanyTable(table, "Company NIP", seller.getNip(), buyer.getNip());
    addRowsCompanyTable(table, "Company BAC", seller.getBankAccoutNumber(),
        buyer.getBankAccoutNumber());

    return table;
  }

  private void addTableHeader(PdfPTable table, Stream<String> headers) {
    headers
        .forEach(columnTitle -> {
          table.addCell(getHeaderCell(columnTitle));
        });
  }

  private PdfPCell getHeaderCell(String content) {
    PdfPCell cell = new PdfPCell();
    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
    cell.setPhrase(new Phrase(content, fonts.getPropertyFont()));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

    return cell;
  }

  private void addRowsCompanyTable(PdfPTable table, String caption, String sellerValue,
      String buyerValue) {

    table.addCell(getHeaderCell(caption));
    table.addCell(sellerValue);
    table.addCell(buyerValue);

  }

  private PdfPTable getProductsTable(List<InvoiceEntry> products) {
    PdfPTable table = new PdfPTable(Configuration.PRODUCTS_TABLE_COLUMNS_COUNT);
    table.setSpacingBefore(Configuration.TABLE_SPACING);
    table.setSpacingAfter(Configuration.TABLE_SPACING);

    addTableHeader(table, Stream
        .of("Name", "Description", "Type", "Amount", "Net Value", "Vat Rate"));

    for (InvoiceEntry entry : products) {
      addRowsProductTable(table, entry);
    }
    return table;
  }


  private void addRowsProductTable(PdfPTable table, InvoiceEntry entry) {
    table.addCell(entry.getProduct().getName());
    table.addCell(entry.getProduct().getDescription());
    table.addCell(entry.getProduct().getProductType().getType());
    table.addCell(Integer.toString(entry.getAmount()));
    table.addCell(entry.getProduct().getNetValue()
        .setScale(Configuration.ROUND_DIGITS_BIG_DECIMAL,
            Configuration.ROUND_MODE_BIG_DECIMAL).toString());
    table.addCell(entry.getProduct().getVatRate().toString());
  }

}
