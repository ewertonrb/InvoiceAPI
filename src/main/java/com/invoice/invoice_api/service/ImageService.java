package com.invoice.invoice_api.service;

import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Set;

@Service
public class ImageService {
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> TYPES = Set.of("image/png", "image/jpeg", "image/webp");
    private final AppUserRepository users; private final CompanyRepository companies; private final CompanyMembershipRepository memberships; private final WorkerProfileRepository workerProfiles; private final AuthenticatedUserService auth; private final CompanyContext context;
    public ImageService(AppUserRepository users, CompanyRepository companies, CompanyMembershipRepository memberships, WorkerProfileRepository workerProfiles, AuthenticatedUserService auth, CompanyContext context) { this.users = users; this.companies = companies; this.memberships = memberships; this.workerProfiles = workerProfiles; this.auth = auth; this.context = context; }
    public void updateAvatar(MultipartFile file) { validate(file); AppUser user = auth.getCurrentUser(); try { user.setAvatarData(file.getBytes()); user.setAvatarContentType(file.getContentType()); users.save(user); } catch (IOException ex) { throw new IllegalArgumentException("Could not read the image.", ex); } }
    public void deleteAvatar() { AppUser user = auth.getCurrentUser(); user.setAvatarData(null); user.setAvatarContentType(null); users.save(user); }
    public ImageData avatar() { AppUser user = users.findById(auth.getCurrentUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found.")); if (user.getAvatarData() == null) throw new ResourceNotFoundException("Avatar not found."); return new ImageData(user.getAvatarContentType(), user.getAvatarData()); }
    @Transactional(readOnly = true)
    public ImageData workerAvatar(Long companyId, Long workerProfileId) {
        validateCompanyMember(companyId);
        var profile = workerProfiles.findById(workerProfileId).orElseThrow(() -> new ResourceNotFoundException("Worker not found."));
        memberships.findByAppUserIdAndCompanyId(profile.getAppUser().getId(), companyId)
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE && m.getRole() == CompanyRole.WORKER)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found."));
        AppUser user = profile.getAppUser();
        if (user.getAvatarData() == null) throw new ResourceNotFoundException("Avatar not found.");
        return new ImageData(user.getAvatarContentType(), user.getAvatarData());
    }
    public void updateLogo(Long companyId, MultipartFile file) { validateManager(companyId); validate(file); Company company = companies.findById(companyId).orElseThrow(() -> new ResourceNotFoundException("Company not found.")); try { company.setLogoData(file.getBytes()); company.setLogoContentType(file.getContentType()); companies.save(company); } catch (IOException ex) { throw new IllegalArgumentException("Could not read the image.", ex); } }
    public void deleteLogo(Long companyId) { validateManager(companyId); Company company = companies.findById(companyId).orElseThrow(() -> new ResourceNotFoundException("Company not found.")); company.setLogoData(null); company.setLogoContentType(null); companies.save(company); }
    public ImageData logo(Long companyId) { if (!companyId.equals(context.getCompanyId())) throw new AccessDeniedBusinessException("The selected company does not match the request."); Company company = companies.findById(companyId).orElseThrow(() -> new ResourceNotFoundException("Company not found.")); if (company.getLogoData() == null) throw new ResourceNotFoundException("Company logo not found."); return new ImageData(company.getLogoContentType(), company.getLogoData()); }
    private void validate(MultipartFile file) { if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES || !TYPES.contains(file.getContentType())) throw new IllegalArgumentException("Use a PNG, JPEG or WebP image up to 5 MB."); }
    private void validateManager(Long companyId) { if (!companyId.equals(context.getCompanyId())) throw new AccessDeniedBusinessException("The selected company does not match the request."); memberships.findByAppUserIdAndCompanyId(auth.getCurrentUserId(), companyId).filter(m -> m.getStatus() == MembershipStatus.ACTIVE && (m.getRole() == CompanyRole.OWNER || m.getRole() == CompanyRole.ADMIN)).orElseThrow(() -> new AccessDeniedBusinessException("Only company administrators can manage the logo.")); }
    private void validateCompanyMember(Long companyId) { if (!companyId.equals(context.getCompanyId())) throw new AccessDeniedBusinessException("The selected company does not match the request."); memberships.findByAppUserIdAndCompanyId(auth.getCurrentUserId(), companyId).filter(m -> m.getStatus() == MembershipStatus.ACTIVE).orElseThrow(() -> new AccessDeniedBusinessException("Active company membership required.")); }
    public record ImageData(String contentType, byte[] bytes) {}
}
