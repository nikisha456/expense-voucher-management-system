package com.abc.expensevoucher.service;

import com.abc.expensevoucher.dto.auth.UserResponse;
import com.abc.expensevoucher.entity.User;
import com.abc.expensevoucher.exception.ResourceNotFoundException;
import com.abc.expensevoucher.mapper.UserMapper;
import com.abc.expensevoucher.repository.UserRepository;
import com.abc.expensevoucher.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse me() {
        return userMapper.toResponse(loadCurrent());
    }

    @Transactional
    public UserResponse uploadSignature(MultipartFile file) {
        User user = loadCurrent();
        String stored = fileStorageService.store(file);
        user.setSignaturePath(stored);
        log.info("User {} updated profile signature", user.getEmail());
        return userMapper.toResponse(userRepository.save(user));
    }

    private User loadCurrent() {
        return userRepository.findById(SecurityUtils.currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
