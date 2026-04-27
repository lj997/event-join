package com.eventjoin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime eventDateTime;
    private String eventLocation;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
    private Boolean isCancelled;
}
