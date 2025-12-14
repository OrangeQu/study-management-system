package com.example.studymanagement.repository;

import com.example.studymanagement.model.LoginDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoginDeviceRepository extends JpaRepository<LoginDevice, Long> {
    List<LoginDevice> findByUserId(Long userId);
    Optional<LoginDevice> findByUserIdAndDeviceKey(Long userId, String deviceKey);
}
