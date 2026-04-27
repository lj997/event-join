package com.eventjoin.controller;

import com.eventjoin.common.ApiResponse;
import com.eventjoin.dto.RegistrationDTO;
import com.eventjoin.entity.Registration;
import com.eventjoin.service.RegistrationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/registrations")
public class AdminRegistrationController {
    
    private final RegistrationService registrationService;
    
    public AdminRegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
    
    @GetMapping("/event/{eventId}")
    public ApiResponse<List<RegistrationDTO>> getRegistrationsByEvent(@PathVariable Long eventId) {
        List<Registration> registrations = registrationService.getRegistrationsByEvent(eventId);
        List<RegistrationDTO> dtos = registrations.stream()
                .map(registrationService::convertToDTO)
                .toList();
        return ApiResponse.success(dtos);
    }
}
