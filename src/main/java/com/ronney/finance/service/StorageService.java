package com.ronney.finance.service;

import com.ronney.finance.service.dto.StorageResult;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StorageResult upload(MultipartFile file);

    void delete(String publicId);
}