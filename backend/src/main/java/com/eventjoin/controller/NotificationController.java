package com.eventjoin.controller;

import com.eventjoin.common.ApiResponse;
import com.eventjoin.common.PageResult;
import com.eventjoin.dto.EventDTO;
import com.eventjoin.dto.NotificationDTO;
import com.eventjoin.entity.Event;
import com.eventjoin.entity.Notification;
import com.eventjoin.service.EventService;
import com.eventjoin.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final EventService eventService;
    
    public NotificationController(NotificationService notificationService,
                                  EventService eventService) {
        this.notificationService = notificationService;
        this.eventService = eventService;
    }
    
    @GetMapping
    public ApiResponse<PageResult<NotificationDTO>> getMyNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Notification> notifications = notificationService.getMyNotifications(page, size);
        PageResult<NotificationDTO> result = PageResult.of(
                notifications.getContent().stream().map(notificationService::convertToDTO).toList(),
                notifications.getTotalElements(),
                notifications.getSize(),
                notifications.getNumber() + 1
        );
        return ApiResponse.success(result);
    }
    
    @GetMapping("/unread")
    public ApiResponse<List<NotificationDTO>> getUnreadNotifications() {
        List<Notification> notifications = notificationService.getUnreadNotifications();
        List<NotificationDTO> dtos = notifications.stream()
                .map(notificationService::convertToDTO)
                .toList();
        return ApiResponse.success(dtos);
    }
    
    @GetMapping("/unread/count")
    public ApiResponse<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return ApiResponse.success(result);
    }
    
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success("已标记为已读", null);
    }
    
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.success("已全部标记为已读", null);
    }
    
    @GetMapping("/upcoming")
    public ApiResponse<List<EventDTO>> getUpcomingEvents() {
        List<Event> events = notificationService.getUpcomingEvents();
        List<EventDTO> dtos = events.stream()
                .map(eventService::convertToDTO)
                .toList();
        return ApiResponse.success(dtos);
    }
}
