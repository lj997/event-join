package com.eventjoin.service;

import com.eventjoin.dto.EventCreateRequest;
import com.eventjoin.dto.EventDTO;
import com.eventjoin.dto.EventUpdateRequest;
import com.eventjoin.entity.Event;
import com.eventjoin.entity.User;
import com.eventjoin.enums.EventStatus;
import com.eventjoin.exception.BusinessException;
import com.eventjoin.repository.EventRepository;
import com.eventjoin.repository.RegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class EventService {
    
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final AuthService authService;
    
    public EventService(EventRepository eventRepository, 
                        RegistrationRepository registrationRepository,
                        AuthService authService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.authService = authService;
    }
    
    @Transactional
    public Event createEvent(EventCreateRequest request) {
        if (request.getDeadline().isAfter(request.getEventDateTime())) {
            throw new BusinessException("报名截止时间不能晚于活动时间");
        }
        
        User currentUser = authService.getCurrentUser();
        
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDateTime(request.getEventDateTime());
        event.setDeadline(request.getDeadline());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setCoverImage(request.getCoverImage());
        event.setStatus(EventStatus.OPEN);
        event.setCreatedBy(currentUser);
        
        return eventRepository.save(event);
    }
    
    @Transactional
    public Event updateEvent(Long id, EventUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getEventDateTime() != null) {
            event.setEventDateTime(request.getEventDateTime());
        }
        if (request.getDeadline() != null) {
            event.setDeadline(request.getDeadline());
        }
        if (request.getMaxParticipants() != null) {
            if (request.getMaxParticipants() < event.getCurrentParticipants()) {
                throw new BusinessException("人数上限不能小于当前已报名人数");
            }
            event.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getCoverImage() != null) {
            event.setCoverImage(request.getCoverImage());
        }
        
        if (event.getDeadline().isAfter(event.getEventDateTime())) {
            throw new BusinessException("报名截止时间不能晚于活动时间");
        }
        
        return eventRepository.save(event);
    }
    
    @Transactional
    public void closeEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        if (event.getStatus() == EventStatus.CLOSED) {
            throw new BusinessException("活动已关闭");
        }
        
        event.setStatus(EventStatus.CLOSED);
        eventRepository.save(event);
    }
    
    public Event getEventById(Long id) {
        return eventRepository.findByIdWithCreator(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
    }
    
    public Page<Event> getAllEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("eventDateTime").descending());
        return eventRepository.findAll(pageable);
    }
    
    public Page<Event> getOpenEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("eventDateTime").ascending());
        return eventRepository.findByStatus(EventStatus.OPEN, pageable);
    }
    
    public Page<Event> searchEvents(String keyword, String location, 
                                    LocalDateTime startDate, LocalDateTime endDate,
                                    int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("eventDateTime").ascending());
        return eventRepository.searchEvents(EventStatus.OPEN, keyword, location, startDate, endDate, pageable);
    }
    
    @Transactional
    public void updateCurrentParticipants(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        long count = registrationRepository.countByEventIdAndIsCancelledFalse(eventId);
        event.setCurrentParticipants((int) count);
        eventRepository.save(event);
    }
    
    public EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setEventDateTime(event.getEventDateTime());
        dto.setDeadline(event.getDeadline());
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setCurrentParticipants(event.getCurrentParticipants());
        dto.setCoverImage(event.getCoverImage());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        
        if (event.getCreatedBy() != null) {
            dto.setCreatedById(event.getCreatedBy().getId());
            dto.setCreatedByName(event.getCreatedBy().getUsername());
        }
        
        return dto;
    }
}
