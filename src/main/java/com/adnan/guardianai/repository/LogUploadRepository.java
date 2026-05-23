package com.adnan.guardianai.repository;

import com.adnan.guardianai.model.LogUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogUploadRepository extends JpaRepository<LogUpload, Long> {
    List<LogUpload> findTop5ByOrderByUploadTimeDesc();
}
