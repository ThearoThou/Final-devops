package com.example.demo;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.service.IDCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private IDCardService idCardService;

    @Test
    void contextLoads() {
        assertNotNull(profileRepository);
        assertNotNull(idCardService);
    }

    @Test
    void testProfileCreation() {
        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("User");
        profile.setEmail("test@example.com");
        profile.setProfileType(ProfileType.USER);
        profile.setDepartment("IT");
        profile.generateUniqueId("2024-IT-0001");

        Profile saved = profileRepository.save(profile);
        assertNotNull(saved.getId());
        assertEquals("2024-IT-0001", saved.getUniqueId());
    }

    @Test
    void testProfileSearch() {
        String query = "test";
        var results = profileRepository.searchProfiles(query);
        assertNotNull(results);
    }
}