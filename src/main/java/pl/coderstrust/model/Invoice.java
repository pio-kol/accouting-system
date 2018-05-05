package pl.coderstrust.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Proxy;
import pl.coderstrust.database.hibernate.LocalDateTimeConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Proxy(lazy = false)
public class Invoice implements WithNameIdIssueDate, WithValidation {

  @ElementCollection(fetch = FetchType.EAGER)
  private List<InvoiceEntry> products;

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  @Column(name = "ID", unique = true, nullable = false)
  private Long id;

  private String name;

  @JoinColumn
  @ManyToOne(cascade = CascadeType.ALL)
  private Company buyer;

  @JoinColumn
  @ManyToOne(cascade = CascadeType.ALL)
  private Company seller;

  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDate issueDate;

  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDate paymentDate;

  @Column(name = "paymentState")
  @Enumerated(EnumType.ORDINAL)
  private PaymentState paymentState;

  public Invoice() {
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  @JsonProperty("invoiceId")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @ApiModelProperty(example = "FV 2/22/06/2019")
  public String getName() {
    return name;
  }

  public void setName(String invoiceName) {
    this.name = invoiceName;
  }

  public Company getBuyer() {
    return buyer;
  }

  public void setBuyer(Company buyer) {
    this.buyer = buyer;
  }

  public Company getSeller() {
    return seller;
  }

  public void setSeller(Company seller) {
    this.seller = seller;
  }

  @ApiModelProperty(example = "2019-06-15")
  public LocalDate getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(String issueDate) {
    this.issueDate = LocalDate.parse(issueDate);
  }

  public void setIssueDate(LocalDate issueDate) {
    this.issueDate = issueDate;
  }

  @ApiModelProperty(example = "2019-07-15")
  public LocalDate getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(String paymentDate) {
    this.paymentDate = LocalDate.parse(paymentDate);
  }

  public void setPaymentDate(LocalDate paymentDate) {
    this.paymentDate = paymentDate;
  }

  public List<InvoiceEntry> getProducts() {
    return products;
  }

  public void setProducts(List<InvoiceEntry> products) {
    this.products = products;
  }

  public PaymentState getPaymentState() {
    return paymentState;
  }

  public void setPaymentState(PaymentState paymentState) {
    this.paymentState = paymentState;
  }

  @Override
  public List<String> validate() {
    List<String> errors = new ArrayList<>();
    errors.addAll(this.getSeller().validate());
    errors.addAll(this.getBuyer().validate());
    errors.addAll(checkDate(this.getIssueDate()));
    errors.addAll(checkDate(this.getPaymentDate()));
    if (this.getProducts().size() == 0) {
      errors.add(Messages.PRODUCTS_LIST_EMPTY);
    } else {
      for (int i = 0; i < this.getProducts().size(); i++) {
        if (this.getProducts().get(i).getAmount() <= 0) {
          errors.add(Messages.PRODUCT_INCORRECT_AMOUNT);
        }
        errors.addAll(this.getProducts().get(i).getProduct().validate());
      }
    }
    if (this.getPaymentState() == null) {
      errors.add(Messages.PAYMENT_STATE_EMPTY);
    }
    return errors;
  }

  @Override
  public boolean equals(Object object) {
    return EqualsBuilder.reflectionEquals(this, object);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, true);
  }
}
