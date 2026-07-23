package com.ronney.finance.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ronney.finance.exception.StorageException;
import com.ronney.finance.service.StorageService;
import com.ronney.finance.service.dto.StorageResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Profile({"dev", "prod"})
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public StorageResult upload(MultipartFile file) {

        try {

            String publicId = "finance-family/avatars/" + UUID.randomUUID();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            return new StorageResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id")
            );

        } catch (IOException e) {
            throw new StorageException("Failed to upload avatar.", e);
        }
    }

    @Override
    public void delete(String publicId) {

        try {

            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

        } catch (IOException e) {
            throw new StorageException(
                    "Failed to delete avatar.",
                    e
            );
        }
    }
}