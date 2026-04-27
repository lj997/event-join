package com.eventjoin.controller;

import com.eventjoin.common.ApiResponse;
import com.eventjoin.common.PageResult;
import com.eventjoin.dto.RegistrationDTO;
import com.eventjoin.entity.Registration;
import com.eventjoin.service.RegistrationService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
    
    @PostMapping("/{eventId}")
    public ApiResponse<RegistrationDTO> register(@PathVariable Long eventId) {
        Registration registration = registrationService.registerForEvent(eventId);
        return ApiResponse.success("报名成功", registrationService.convertToDTO(registration));
    }
    
    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> cancel(@PathVariable Long eventId) {
        registrationService.cancelRegistration(eventId);
        return ApiResponse.success("取消报名成功", null);
    }
    
    @GetMapping("/check/{eventId}")
    public ApiResponse<Boolean> checkRegistration(@PathVariable Long eventId) {
        boolean isRegistered = registrationService.isRegistered(eventId);
        return ApiResponse.success(isRegistered);
    }
    
    @GetMapping("/my")
    public ApiResponse<PageResult<RegistrationDTO>> getMyRegistrations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Registration> registrations = registrationService.getMyRegistrations(page, size);
        PageResult<RegistrationDTO> result = PageResult.of(
                registrations.getContent().stream().map(registrationService::convertToDTO).toList(),
                registrations.getTotalElements(),
                registrations.getSize(),
                registrations.getNumber() + 1
        );
        return ApiResponse.success(result);
    }
}
