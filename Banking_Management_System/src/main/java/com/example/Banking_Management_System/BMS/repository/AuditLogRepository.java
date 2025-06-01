package com.example.Banking_Management_System.BMS.repository;

import com.example.Banking_Management_System.BMS.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}
