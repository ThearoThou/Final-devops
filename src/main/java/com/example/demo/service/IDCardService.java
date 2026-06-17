package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.repository.ProfileRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer; // 1. Added import for Barcode support
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class IDCardService {
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Value("${upload.path:uploads/}")
    private String uploadPath;
    
    public Profile createProfile(Profile profile, MultipartFile photo) throws IOException {
        System.out.println("=== IDCardService.createProfile ===");
        if (photo != null && !photo.isEmpty()) {
            try {
                System.out.println("Processing photo: " + photo.getOriginalFilename());
                String fileName = savePhoto(photo);
                profile.setPhotoPath(fileName);
                System.out.println("✅ Photo saved: " + fileName);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Photo validation failed: " + e.getMessage());
                throw e;
            } catch (Exception e) {
                System.err.println("⚠️ Error saving photo: " + e.getMessage());
                e.printStackTrace();
                profile.setPhotoPath(null);
            }
        } else {
            System.out.println("ℹ️ No photo provided");
        }
        
        if (profile.getUniqueId() == null || profile.getUniqueId().isEmpty()) {
            profile.generateUniqueId(null);
            System.out.println("✅ Generated unique ID: " + profile.getUniqueId());
        }
        
        Profile savedProfile = profileRepository.save(profile);
        System.out.println("✅ Profile saved with ID: " + savedProfile.getId());
        return savedProfile;
    }
    
    private String savePhoto(MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("No file selected. Please choose a photo.");
        }
        String contentType = photo.getContentType();
        String originalFilename = photo.getOriginalFilename();
        long fileSize = photo.getSize();
        
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPEG and PNG files are allowed. Received: " + contentType);
        }
        
        long maxSize = 5 * 1024 * 1024;
        if (fileSize > maxSize) {
            throw new IllegalArgumentException("File size exceeds 5MB. Current size: " + (fileSize / 1024) + " KB");
        }
        
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, photo.getBytes());
        return fileName;
    }
    
    // Generate QR Code
    public byte[] generateQRCode(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
    
    // 2. NEW METHOD: Generate Code-128 Linear Barcode
    public byte[] generateBarcode(String content) throws WriterException, IOException {
        Code128Writer barcodeWriter = new Code128Writer();
        // Generates an asset tracking 1D layout (300px wide, 60px tall)
        BitMatrix bitMatrix = barcodeWriter.encode(content, BarcodeFormat.CODE_128, 300, 60);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
    
    // Generate PDF ID Card with both elements
    public byte[] generatePDFIDCard(Profile profile, Template template) throws IOException, WriterException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        document.add(new Paragraph("ID CARD").setFontSize(24).setBold());
        
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        
        if (profile.getPhotoPath() != null && !profile.getPhotoPath().isEmpty()) {
            try {
                File photoFile = new File(uploadPath + profile.getPhotoPath());
                if (photoFile.exists()) {
                    BufferedImage bufferedImage = ImageIO.read(photoFile);
                    if (bufferedImage != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "png", baos);
                        Image img = new Image(com.itextpdf.io.image.ImageDataFactory.create(baos.toByteArray()));
                        img.setWidth(100);
                        img.setHeight(100);
                        table.addCell(img);
                    } else {
                        table.addCell("No Image");
                    }
                } else {
                    table.addCell("No Image");
                }
            } catch (IOException e) {
                table.addCell("No Image");
            }
        } else {
            table.addCell("No Image");
        }
        
        table.addCell("ID: " + profile.getUniqueId());
        table.addCell("Name: " + profile.getFirstName() + " " + profile.getLastName());
        table.addCell("Type: " + profile.getProfileType());
        table.addCell("Department: " + profile.getDepartment());
        table.addCell("Email: " + profile.getEmail());
        table.addCell("Phone: " + (profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "N/A"));
        
        document.add(table);
        
        // Add QR Code to PDF
        String qrContent = "ID: " + profile.getUniqueId() + 
                          "\nName: " + profile.getFirstName() + " " + profile.getLastName() +
                          "\nType: " + profile.getProfileType() +
                          "\nEmail: " + profile.getEmail();
        
        byte[] qrCodeBytes = generateQRCode(qrContent);
        Image qrImage = new Image(com.itextpdf.io.image.ImageDataFactory.create(qrCodeBytes));
        qrImage.setWidth(100);
        qrImage.setHeight(100);
        document.add(new Paragraph("\nVerification QR Code:"));
        document.add(qrImage);
        
        // 3. Added Scannable Barcode into the PDF Output
        byte[] barcodeBytes = generateBarcode(profile.getUniqueId());
        Image barcodeImage = new Image(com.itextpdf.io.image.ImageDataFactory.create(barcodeBytes));
        barcodeImage.setWidth(180);
        barcodeImage.setHeight(40);
        document.add(new Paragraph("\nAsset Barcode (Code-128):"));
        document.add(barcodeImage);
        
        document.close();
        return byteArrayOutputStream.toByteArray();
    }
    
    public List<Profile> batchCreateProfiles(List<Profile> profiles) {
        for (Profile profile : profiles) {
            if (profile.getUniqueId() == null || profile.getUniqueId().isEmpty()) {
                profile.generateUniqueId(null);
            }
        }
        return profileRepository.saveAll(profiles);
    }
    
    public Profile getProfileById(Long id) {
        return profileRepository.findById(id).orElseThrow(() -> new RuntimeException("Profile not found with ID: " + id));
    }
    
    public Profile updateProfile(Long id, Profile profileDetails, MultipartFile photo) throws IOException {
        Profile existingProfile = getProfileById(id);
        existingProfile.setFirstName(profileDetails.getFirstName());
        existingProfile.setLastName(profileDetails.getLastName());
        existingProfile.setEmail(profileDetails.getEmail());
        existingProfile.setPhoneNumber(profileDetails.getPhoneNumber());
        existingProfile.setProfileType(profileDetails.getProfileType());
        existingProfile.setDepartment(profileDetails.getDepartment());
        existingProfile.setPosition(profileDetails.getPosition());
        existingProfile.setAddress(profileDetails.getAddress());
        existingProfile.setDateOfBirth(profileDetails.getDateOfBirth());
        existingProfile.setUpdatedAt(LocalDateTime.now());
        
        if (photo != null && !photo.isEmpty()) {
            String fileName = savePhoto(photo);
            existingProfile.setPhotoPath(fileName);
        }
        return profileRepository.save(existingProfile);
    }
    
    public void deleteProfile(Long id) {
        Profile profile = getProfileById(id);
        if (profile.getPhotoPath() != null && !profile.getPhotoPath().isEmpty()) {
            try {
                Path photoPath = Paths.get(uploadPath + profile.getPhotoPath());
                Files.deleteIfExists(photoPath);
            } catch (IOException e) {
                System.err.println("⚠️ Could not delete photo: " + e.getMessage());
            }
        }
        profileRepository.deleteById(id);
    }
    
    public List<Profile> getAllProfiles() { return profileRepository.findAll(); }
    public List<Profile> searchProfiles(String query) {
        if (query == null || query.trim().isEmpty()) return getAllProfiles();
        return profileRepository.searchProfiles(query.trim());
    }
    public List<Profile> getProfilesByType(ProfileType type) { return profileRepository.findByProfileType(type); }
}