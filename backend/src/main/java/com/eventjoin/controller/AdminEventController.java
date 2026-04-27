package com.eventjoin.controller;

import com.eventjoin.common.ApiResponse;
import com.eventjoin.common.PageResult;
import com.eventjoin.dto.EventCreateRequest;
import com.eventjoin.dto.EventDTO;
import com.eventjoin.dto.EventUpdateRequest;
import com.eventjoin.entity.Event;
import com.eventjoin.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {
    
    private final EventService eventService;
    
    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    @PostMapping
    public ApiResponse<EventDTO> createEvent(@Valid @RequestBody EventCreateRequest request) {
        Event event = eventService.createEvent(request);
        return ApiResponse.success("活动创建成功", eventService.convertToDTO(event));
    }
    
    @PutMapping("/{id}")
    public ApiResponse<EventDTO> updateEvent(@PathVariable Long id, 
                                              @Valid @RequestBody EventUpdateRequest request) {
        Event event = eventService.updateEvent(id, request);
        return ApiResponse.success("活动更新成功", eventService.convertToDTO(event));
    }
    
    @PutMapping("/{id}/close")
    public ApiResponse<Void> closeEvent(@PathVariable Long id) {
        eventService.closeEvent(id);
        return ApiResponse.success("活动已关闭", null);
    }
    
    @GetMapping
    public ApiResponse<PageResult<EventDTO>> getAllEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Event> events = eventService.getAllEvents(page, size);
        PageResult<EventDTO> result = PageResult.of(
                events.getContent().stream().map(eventService::convertToDTO).toList(),
                events.getTotalElements(),
                events.getSize(),
                events.getNumber() + 1
        );
        return ApiResponse.success(result);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<EventDTO> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ApiResponse.success(eventService.convertToDTO(event));
    }
}
