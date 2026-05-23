package com.adnan.guardianai.repository;

import com.adnan.guardianai.model.FileScanResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileScanResultRepository extends JpaRepository<FileScanResult, Long> {
    List<FileScanResult> findTop10ByOrderByScannedAtDesc();
}
