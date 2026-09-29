package com.eve.healthcare.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "centre_tests",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_centre_test", columnNames = {"centre_id", "test_id"})
    }
)
public class CentreTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centre_id", nullable = false)
    private DiagnosticCentre centre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private DiagnosticTest test;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "centreTest")
    private List<Booking> bookings = new ArrayList<>();

    public CentreTest() {
    }

    public CentreTest(DiagnosticCentre centre, DiagnosticTest test, BigDecimal price) {
        this.centre = centre;
        this.test = test;
        this.price = price;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiagnosticCentre getCentre() {
        return centre;
    }

    public void setCentre(DiagnosticCentre centre) {
        this.centre = centre;
    }

    public DiagnosticTest getTest() {
        return test;
    }

    public void setTest(DiagnosticTest test) {
        this.test = test;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
