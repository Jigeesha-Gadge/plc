package com.plc.user.repository;

import com.plc.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User , Long>{

    Optional<User> findByMobileNumber(String id);

    boolean existsByUsername(String username);

    boolean existsByMobileNumber(String mobileNumber);
}
