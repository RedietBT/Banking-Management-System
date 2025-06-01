package com.example.Banking_Management_System.BMS.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private LocalDateTime timestamp;
    private Long userId;
    private String username;
    private String actionType;
    private String details;
}
