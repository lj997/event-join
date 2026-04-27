package com.eventjoin.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "event_id"})
})
public class Registration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @Column(nullable = false)
    private LocalDateTime registeredAt;
    
    private LocalDateTime cancelledAt;
    
    private Boolean isCancelled = false;
    
    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        if (isCancelled == null) {
            isCancelled = false;
        }
    }
}
