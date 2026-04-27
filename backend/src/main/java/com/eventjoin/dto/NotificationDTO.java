package com.eventjoin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
