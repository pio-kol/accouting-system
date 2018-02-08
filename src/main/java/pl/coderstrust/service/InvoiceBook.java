package pl.coderstrust.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import pl.coderstrust.database.Database;
import pl.coderstrust.database.database.memory.InMemoryDatabase;
import pl.coderstrust.model.Company;
import pl.coderstrust.model.Invoice;
import pl.coderstrust.model.InvoiceEntry;
import pl.coderstrust.model.PaymentState;

public class InvoiceBook {

  private Database database = new InMemoryDatabase();
  private static long currentInvoiceNumber = 0;

  /**
   * Method add Object Invoice to db.
   */
  public void addInvoice(String idVisible, Company buyer, Company seller,
      int issueDateDay, int issueDateMonth, int issueDateYear,
      List<InvoiceEntry> products, PaymentState paymentState) {

    // validateVisibleId(); cant already exist in db

    LocalDate temp = LocalDate.of(issueDateYear, issueDateMonth, issueDateDay);

    Invoice invoice = new Invoice(idVisible, buyer, seller,
        temp, temp.plusDays(15), products, paymentState);

    invoice.setSystemId(invoiceSystemIdGenerator());
    database.addInvoice(invoice);
    currentInvoiceNumber++;
  }

  public void addInvoice(Invoice invoice){
    invoice.setSystemId(invoiceSystemIdGenerator());
    database.addInvoice(invoice);
    currentInvoiceNumber++;
  }

  public void removeInvoice(String idVisible) {
    database.deleteInvoiceById(getIdSystemByIdVisible(idVisible));
  }

  public Invoice findInvoice(String idVisible) {
    return database.getInvoiceById(getIdSystemByIdVisible(idVisible));
  }

  public void updateInovoice(Invoice invoice) {
    removeInvoice(invoice.getVisibleId());
    database.addInvoice(invoice);
  }



  private long invoiceSystemIdGenerator() {
    return currentInvoiceNumber + 1;
  }

  //!!
  private boolean validateVisibleId() {
    return false;
  }

  private long getIdSystemByIdVisible(String idVisible) {
    List<Invoice> list = database.getInvoices();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).getVisibleId().equals(idVisible)) {
        return list.get(i).getSystemId();
      }
    }
    throw new NoSuchElementException("There is no invoice with id : " + idVisible);
  }
}
