package pl.coderstrust.e2e.model;

import lombok.Getter;

@Getter
public enum PaymentState {
  PAID("Paid"),
  NOT_PAID("Not paid");

  private final String state;

  PaymentState(String state) {
    this.state = state;
  }
}
