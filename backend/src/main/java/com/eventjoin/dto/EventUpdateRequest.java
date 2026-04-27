package com.eventjoin.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventUpdateRequest {
    
    private String title;
    
    private String description;
    
    private String location;
    
    private LocalDateTime eventDateTime;
    
    private LocalDateTime deadline;
    
    @Positive(message = "人数上限必须是正数")
    private Integer maxParticipants;
    
    private String coverImage;
}
