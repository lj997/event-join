package com.eventjoin.controller;

import com.eventjoin.common.ApiResponse;
import com.eventjoin.common.PageResult;
import com.eventjoin.dto.EventDTO;
import com.eventjoin.entity.Event;
import com.eventjoin.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/events")
public class EventController {
    
    private final EventService eventService;
    
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    @GetMapping("/public/list")
    public ApiResponse<PageResult<EventDTO>> getOpenEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Event> events = eventService.getOpenEvents(page, size);
        PageResult<EventDTO> result = PageResult.of(
                events.getContent().stream().map(eventService::convertToDTO).toList(),
                events.getTotalElements(),
                events.getSize(),
                events.getNumber() + 1
        );
        return ApiResponse.success(result);
    }
    
    @GetMapping("/public/search")
    public ApiResponse<PageResult<EventDTO>> searchEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        
        Page<Event> events = eventService.searchEvents(keyword, location, startDateTime, endDateTime, page, size);
        PageResult<EventDTO> result = PageResult.of(
                events.getContent().stream().map(eventService::convertToDTO).toList(),
                events.getTotalElements(),
                events.getSize(),
                events.getNumber() + 1
        );
        return ApiResponse.success(result);
    }
    
    @GetMapping("/public/{id}")
    public ApiResponse<EventDTO> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ApiResponse.success(eventService.convertToDTO(event));
    }
}
