package com.eventjoin.service;

import com.eventjoin.dto.NotificationDTO;
import com.eventjoin.entity.Event;
import com.eventjoin.entity.Notification;
import com.eventjoin.entity.Registration;
import com.eventjoin.entity.User;
import com.eventjoin.enums.EventStatus;
import com.eventjoin.exception.BusinessException;
import com.eventjoin.repository.EventRepository;
import com.eventjoin.repository.NotificationRepository;
import com.eventjoin.repository.RegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final AuthService authService;
    
    public NotificationService(NotificationRepository notificationRepository,
                               EventRepository eventRepository,
                               RegistrationRepository registrationRepository,
                               AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.authService = authService;
    }
    
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<Event> upcomingEvents = eventRepository.findEventsStartingSoon(
                EventStatus.OPEN, now, tomorrow);
        
        for (Event event : upcomingEvents) {
            List<Registration> registrations = registrationRepository.findAllActiveByEventId(event.getId());
            
            for (Registration registration : registrations) {
                sendReminder(registration.getUser(), event);
            }
        }
    }
    
    private void sendReminder(User user, Event event) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEvent(event);
        notification.setTitle("活动即将开始提醒");
        notification.setContent("您报名的活动「" + event.getTitle() + "」将于明天开始，请准时参加！\n" +
                "活动地点：" + (event.getLocation() != null ? event.getLocation() : "待定") + "\n" +
                "活动时间：" + event.getEventDateTime());
        notification.setIsRead(false);
        
        notificationRepository.save(notification);
    }
    
    public Page<Notification> getMyNotifications(int page, int size) {
        User currentUser = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return notificationRepository.findByUserIdWithEvent(currentUser.getId(), pageable);
    }
    
    public List<Notification> getUnreadNotifications() {
        User currentUser = authService.getCurrentUser();
        return notificationRepository.findByUserIdAndIsRead(currentUser.getId(), false);
    }
    
    public long getUnreadCount() {
        User currentUser = authService.getCurrentUser();
        return notificationRepository.countByUserIdAndIsRead(currentUser.getId(), false);
    }
    
    @Transactional
    public void markAsRead(Long notificationId) {
        User currentUser = authService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权限操作此通知");
        }
        
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }
    
    @Transactional
    public void markAllAsRead() {
        User currentUser = authService.getCurrentUser();
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(
                currentUser.getId(), false);
        
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : unread) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }
        
        notificationRepository.saveAll(unread);
    }
    
    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        return eventRepository.findEventsStartingSoon(EventStatus.OPEN, now, tomorrow);
    }
    
    public NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        
        if (notification.getEvent() != null) {
            dto.setEventId(notification.getEvent().getId());
            dto.setEventTitle(notification.getEvent().getTitle());
        }
        
        return dto;
    }
}
