package com.eventjoin.dto;

import com.eventjoin.enums.EventStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime eventDateTime;
    private LocalDateTime deadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String coverImage;
    private EventStatus status;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
