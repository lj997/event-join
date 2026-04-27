package com.eventjoin.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventCreateRequest {
    
    @NotBlank(message = "活动标题不能为空")
    private String title;
    
    private String description;
    
    private String location;
    
    @NotNull(message = "活动时间不能为空")
    @Future(message = "活动时间必须是未来时间")
    private LocalDateTime eventDateTime;
    
    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime deadline;
    
    @NotNull(message = "人数上限不能为空")
    @Positive(message = "人数上限必须是正数")
    private Integer maxParticipants;
    
    private String coverImage;
}
