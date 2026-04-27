package com.eventjoin.service;

import com.eventjoin.dto.RegistrationDTO;
import com.eventjoin.entity.Event;
import com.eventjoin.entity.Registration;
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
import java.util.List;

@Service
public class RegistrationService {
    
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final AuthService authService;
    
    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               AuthService authService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.authService = authService;
    }
    
    @Transactional
    public Registration registerForEvent(Long eventId) {
        User currentUser = authService.getCurrentUser();
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        if (event.getStatus() != EventStatus.OPEN) {
            throw new BusinessException("活动已关闭，无法报名");
        }
        
        if (LocalDateTime.now().isAfter(event.getDeadline())) {
            throw new BusinessException("报名已截止");
        }
        
        Optional<Registration> existingRegOpt = registrationRepository.findByUserAndEvent(currentUser, event);
        
        if (existingRegOpt.isPresent()) {
            Registration existingReg = existingRegOpt.get();
            if (!existingReg.getIsCancelled()) {
                throw new BusinessException("您已报名过此活动");
            }
            
            long currentCount = registrationRepository.countByEventIdAndIsCancelledFalse(eventId);
            if (currentCount >= event.getMaxParticipants()) {
                throw new BusinessException("报名人数已达上限");
            }
            
            existingReg.setIsCancelled(false);
            existingReg.setRegisteredAt(LocalDateTime.now());
            existingReg.setCancelledAt(null);
            
            Registration saved = registrationRepository.save(existingReg);
            
            event.setCurrentParticipants((int) (currentCount + 1));
            eventRepository.save(event);
            
            return saved;
        }
        
        long currentCount = registrationRepository.countByEventIdAndIsCancelledFalse(eventId);
        if (currentCount >= event.getMaxParticipants()) {
            throw new BusinessException("报名人数已达上限");
        }
        
        Registration registration = new Registration();
        registration.setUser(currentUser);
        registration.setEvent(event);
        registration.setIsCancelled(false);
        
        Registration saved = registrationRepository.save(registration);
        
        event.setCurrentParticipants((int) (currentCount + 1));
        eventRepository.save(event);
        
        return saved;
    }
    
    @Transactional
    public void cancelRegistration(Long eventId) {
        User currentUser = authService.getCurrentUser();
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        Registration registration = registrationRepository.findByUserAndEvent(currentUser, event)
                .orElseThrow(() -> new BusinessException("您未报名此活动"));
        
        if (registration.getIsCancelled()) {
            throw new BusinessException("已取消报名，无需重复操作");
        }
        
        if (LocalDateTime.now().isAfter(event.getEventDateTime())) {
            throw new BusinessException("活动已开始，无法取消报名");
        }
        
        registration.setIsCancelled(true);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);
        
        long currentCount = registrationRepository.countByEventIdAndIsCancelledFalse(eventId);
        event.setCurrentParticipants((int) currentCount);
        eventRepository.save(event);
    }
    
    public boolean isRegistered(Long eventId) {
        User currentUser = authService.getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        return registrationRepository.existsByUserAndEventAndIsCancelledFalse(currentUser, event);
    }
    
    public Page<Registration> getMyRegistrations(int page, int size) {
        User currentUser = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("registeredAt").descending());
        return registrationRepository.findByUserIdWithEvent(currentUser.getId(), false, pageable);
    }
    
    public List<Registration> getRegistrationsByEvent(Long eventId) {
        return registrationRepository.findByEventIdWithUser(eventId, false);
    }
    
    public List<Registration> getActiveRegistrationsByEvent(Long eventId) {
        return registrationRepository.findAllActiveByEventId(eventId);
    }
    
    public RegistrationDTO convertToDTO(Registration registration) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setId(registration.getId());
        dto.setRegisteredAt(registration.getRegisteredAt());
        dto.setCancelledAt(registration.getCancelledAt());
        dto.setIsCancelled(registration.getIsCancelled());
        
        if (registration.getUser() != null) {
            dto.setUserId(registration.getUser().getId());
            dto.setUsername(registration.getUser().getUsername());
            dto.setEmail(registration.getUser().getEmail());
            dto.setPhone(registration.getUser().getPhone());
        }
        
        if (registration.getEvent() != null) {
            dto.setEventId(registration.getEvent().getId());
            dto.setEventTitle(registration.getEvent().getTitle());
            dto.setEventDateTime(registration.getEvent().getEventDateTime());
            dto.setEventLocation(registration.getEvent().getLocation());
        }
        
        return dto;
    }
}
