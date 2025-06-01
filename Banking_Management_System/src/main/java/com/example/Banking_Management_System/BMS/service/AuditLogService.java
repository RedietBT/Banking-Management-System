package com.example.Banking_Management_System.BMS.service;

import com.example.Banking_Management_System.BMS.DTO.AuditLogResponse;
import com.example.Banking_Management_System.BMS.model.AuditLog;
import com.example.Banking_Management_System.BMS.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW) // Ensures audit log is saved independently
    public void logAction(Long userId, String username, String actionType, String details, String targetEntityType, Long targetEntityId) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .actionType(actionType)
                .details(details)
                .targetEntityType(targetEntityType)
                .targetEntityId(targetEntityId)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        return auditLogs.stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .timestamp(auditLog.getTimestamp())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .actionType(auditLog.getActionType())
                .details(auditLog.getDetails())
                .build();
    }
}

