package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileBuilder;
import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import com.example.demo.service.IDCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/id-card")
public class IDCardController {
    
    @Autowired
    private IDCardService idCardService;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    // Show create profile form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("profileTypes", ProfileType.values());
        return "create-profile";
    }
    
    // Create profile with photo parameter matching the frontend file input name
    @PostMapping("/create")
    public String createProfile(@ModelAttribute Profile profile,
                               @RequestParam(value = "photo", required = false) MultipartFile photo,
                               Model model) {
        try {
            System.out.println("=== CREATE PROFILE ===");
            System.out.println("Name: " + profile.getFirstName() + " " + profile.getLastName());
            System.out.println("Email: " + profile.getEmail());
            System.out.println("Department: " + profile.getDepartment());
            
            // Check photo
            if (photo == null) {
                System.out.println("❌ Photo is NULL");
            } else if (photo.isEmpty()) {
                System.out.println("❌ Photo is EMPTY");
            } else {
                System.out.println("✅ Photo received: " + photo.getOriginalFilename());
                System.out.println("   Size: " + photo.getSize() + " bytes");
                System.out.println("   Type: " + photo.getContentType());
            }
            
            // Build profile using ProfileBuilder matching our updated LocalDate types
            ProfileBuilder builder = ProfileBuilder.builder()
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .email(profile.getEmail())
                    .phoneNumber(profile.getPhoneNumber())
                    .profileType(profile.getProfileType())
                    .department(profile.getDepartment())
                    .position(profile.getPosition())
                    .address(profile.getAddress())
                    .dateOfBirth(profile.getDateOfBirth())
                    .photoPath(profile.getPhotoPath());
            
            Profile builtProfile = builder.build();
            
            // Save profile with photo (if provided)
            Profile savedProfile = idCardService.createProfile(builtProfile, photo);
            
            model.addAttribute("profile", savedProfile);
            model.addAttribute("success", "✅ Profile created successfully! ID: " + savedProfile.getUniqueId());
            
            return "redirect:/id-card/preview/" + savedProfile.getId();
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Validation error: " + e.getMessage());
            model.addAttribute("error", "❌ " + e.getMessage());
            model.addAttribute("profileTypes", ProfileType.values());
            return "create-profile";
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "❌ Error creating profile: " + e.getMessage());
            model.addAttribute("profileTypes", ProfileType.values());
            return "create-profile";
        }
    }
    
    // Show profile preview
    @GetMapping("/preview/{id}")
    public String showPreview(@PathVariable Long id, Model model) {
        try {
            Profile profile = idCardService.getProfileById(id);
            Template defaultTemplate = templateRepository.findByIsDefaultTrue();
            
            model.addAttribute("profile", profile);
            model.addAttribute("template", defaultTemplate);
            model.addAttribute("preview", true);
            
            return "preview-id-card";
        } catch (Exception e) {
            model.addAttribute("error", "Profile not found");
            return "list-profiles";
        }
    }
    
    // Generate PDF
    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generatePDF(@PathVariable Long id) {
        try {
            Profile profile = idCardService.getProfileById(id);
            Template template = templateRepository.findByIsDefaultTrue();
            
            byte[] pdfBytes = idCardService.generatePDFIDCard(profile, template);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "id-card-" + profile.getUniqueId() + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // List all profiles
    @GetMapping("/list")
    public String listProfiles(@RequestParam(required = false) String search, Model model) {
        List<Profile> profiles;
        if (search != null && !search.isEmpty()) {
            profiles = idCardService.searchProfiles(search);
        } else {
            profiles = idCardService.getAllProfiles();
        }
        
        model.addAttribute("profiles", profiles);
        model.addAttribute("profileTypes", ProfileType.values());
        return "list-profiles";
    }
    
    // Batch create profiles
    @PostMapping("/batch")
    public String batchCreateProfiles(@RequestParam String firstName,
                                     @RequestParam String lastName,
                                     @RequestParam String email,
                                     @RequestParam String department,
                                     @RequestParam String profileType,
                                     @RequestParam Integer count,
                                     Model model) {
        try {
            ProfileType type = ProfileType.valueOf(profileType.toUpperCase());
            List<Profile> profiles = new java.util.ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                Profile profile = new Profile();
                profile.setFirstName(firstName + (i + 1));
                profile.setLastName(lastName);
                profile.setEmail(email.split("@")[0] + (i + 1) + "@" + email.split("@")[1]);
                profile.setProfileType(type);
                profile.setDepartment(department);
                profile.generateUniqueId(null);
                profiles.add(profile);
            }
            
            List<Profile> savedProfiles = idCardService.batchCreateProfiles(profiles);
            model.addAttribute("success", "✅ Created " + savedProfiles.size() + " profiles successfully!");
            return listProfiles(null, model);
        } catch (Exception e) {
            model.addAttribute("error", "❌ " + e.getMessage());
            return "list-profiles";
        }
    }
    
    // Delete profile
    @PostMapping("/delete/{id}")
    public String deleteProfile(@PathVariable Long id, Model model) {
        try {
            idCardService.deleteProfile(id);
            model.addAttribute("success", "✅ Profile deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "❌ Error deleting profile: " + e.getMessage());
        }
        return listProfiles(null, model);
    }
    
    // Search profiles (AJAX)
    @GetMapping("/search")
    @ResponseBody
    public List<Profile> searchProfiles(@RequestParam String query) {
        return idCardService.searchProfiles(query);
    }

    // Endpoint to stream QR Code image to live-preview layout page
    @GetMapping("/qrcode/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getLivePreviewQRCode(@PathVariable Long id) {
        try {
            Profile profile = idCardService.getProfileById(id);
            String qrContent = "ID: " + profile.getUniqueId() + 
                              "\nName: " + profile.getFirstName() + " " + profile.getLastName() +
                              "\nDepartment: " + profile.getDepartment();
            
            byte[] qrBytes = idCardService.generateQRCode(qrContent);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint to stream Scannable Barcode image to live-preview layout page
    @GetMapping("/barcode/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getLivePreviewBarcode(@PathVariable Long id) {
        try {
            Profile profile = idCardService.getProfileById(id);
            byte[] barcodeBytes = idCardService.generateBarcode(profile.getUniqueId());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(barcodeBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}