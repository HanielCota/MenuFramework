package dev.haniel.menu.example.domain;

/** A non-negative example currency amount. */
public record Price(int value) {

  public Price {
    if (value < 0) {
      throw new IllegalArgumentException("Price cannot be negative");
    }
  }

  public String formatted() {
    return value + " coins";
  }
}
