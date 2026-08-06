package com.invoice.invoice_api.controller;
import com.invoice.invoice_api.service.ImageService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {
    private final ImageService images;
    public ImageController(ImageService images) { this.images = images; }
    @GetMapping("/users/me/avatar") public ResponseEntity<byte[]> avatar() { return image(images.avatar()); }
    @GetMapping("/companies/{companyId}/workers/{workerProfileId}/avatar") public ResponseEntity<byte[]> workerAvatar(@PathVariable Long companyId, @PathVariable Long workerProfileId) { return image(images.workerAvatar(companyId, workerProfileId)); }
    @PutMapping(value = "/users/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<Void> updateAvatar(@RequestPart("file") MultipartFile file) { images.updateAvatar(file); return ResponseEntity.noContent().build(); }
    @DeleteMapping("/users/me/avatar") public ResponseEntity<Void> deleteAvatar() { images.deleteAvatar(); return ResponseEntity.noContent().build(); }
    @GetMapping("/companies/{companyId}/logo") public ResponseEntity<byte[]> logo(@PathVariable Long companyId) { return image(images.logo(companyId)); }
    @PutMapping(value = "/companies/{companyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<Void> updateLogo(@PathVariable Long companyId, @RequestPart("file") MultipartFile file) { images.updateLogo(companyId, file); return ResponseEntity.noContent().build(); }
    @DeleteMapping("/companies/{companyId}/logo") public ResponseEntity<Void> deleteLogo(@PathVariable Long companyId) { images.deleteLogo(companyId); return ResponseEntity.noContent().build(); }
    private ResponseEntity<byte[]> image(ImageService.ImageData image) { return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType())).cacheControl(CacheControl.noCache()).body(image.bytes()); }
}
