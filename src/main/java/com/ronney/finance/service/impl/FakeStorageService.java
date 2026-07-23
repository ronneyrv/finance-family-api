package com.ronney.finance.service.impl;

import com.ronney.finance.service.StorageService;
import com.ronney.finance.service.dto.StorageResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("test")
public class FakeStorageService implements StorageService {

    @Override
    public StorageResult upload(MultipartFile file) {
        return new StorageResult(
                "https://fake-storage/avatar.jpg",
                "fake-public-id"
        );
    }

    @Override
    public void delete(String publicId) {
        // no-op
    }
}