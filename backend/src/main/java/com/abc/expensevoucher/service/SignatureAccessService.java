package com.abc.expensevoucher.service;

import com.abc.expensevoucher.entity.Role;
import com.abc.expensevoucher.entity.User;
import com.abc.expensevoucher.exception.ResourceNotFoundException;
import com.abc.expensevoucher.repository.UserRepository;
import com.abc.expensevoucher.repository.VoucherRepository;
import com.abc.expensevoucher.security.UserPrincipal;
import com.abc.expensevoucher.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class SignatureAccessService {

    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public SignatureFile load(String fileName) {
        UserPrincipal principal = SecurityUtils.currentUser();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Path path = fileStorageService.resolve(fileName);
        boolean ownProfile = path.getFileName().toString().equals(user.getSignaturePath());
        Long ownerFilter = principal.getRole() == Role.EMPLOYEE ? principal.getId() : null;
        boolean onVisibleVoucher = voucherRepository.countVisibleSignatures(path.getFileName().toString(), ownerFilter) > 0;
        if (!ownProfile && !onVisibleVoucher) {
            throw new ResourceNotFoundException("File not found");
        }
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("File not found");
        }
        Resource resource = new FileSystemResource(path);
        return new SignatureFile(resource, fileStorageService.mediaType(path.getFileName().toString()));
    }

    public record SignatureFile(Resource resource, String mediaType) {
    }
}
