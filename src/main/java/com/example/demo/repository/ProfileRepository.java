package com.example.demo.repository;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    Optional<Profile> findByUniqueId(String uniqueId);
    
    Optional<Profile> findByEmail(String email);
    
    List<Profile> findByProfileType(ProfileType profileType);
    
    List<Profile> findByDepartment(String department);
    
    @Query("SELECT p FROM Profile p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.uniqueId) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Profile> searchProfiles(@Param("query") String query);
    
    boolean existsByEmail(String email);
    
    boolean existsByUniqueId(String uniqueId);
    
    List<Profile> findByActiveTrue();
}