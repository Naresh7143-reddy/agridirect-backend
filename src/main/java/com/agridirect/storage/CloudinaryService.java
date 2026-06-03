package com.agridirect.storage;

import com.agridirect.common.exception.ApiException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired private Cloudinary cloudinary;

    public String uploadProductImage(MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "agridirect/products",
                            "resource_type", "image",
                            "quality", "auto",
                            "fetch_format", "auto"));
            return result.get("secure_url").toString();
        } catch (Exception e) {
            throw new ApiException("Failed to upload product image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadProfilePhoto(MultipartFile file, String userId) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "agridirect/profiles",
                            "public_id", "profile_" + userId,
                            "overwrite", true,
                            "width", 400,
                            "height", 400,
                            "crop", "fill",
                            "resource_type", "image",
                            "quality", "auto"));
            return result.get("secure_url").toString();
        } catch (Exception e) {
            throw new ApiException("Failed to upload profile photo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadDiseasePhoto(MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "agridirect/disease",
                            "resource_type", "image",
                            "quality", "auto"));
            return result.get("secure_url").toString();
        } catch (Exception e) {
            throw new ApiException("Failed to upload disease photo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadKycDocument(MultipartFile file, String userId) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "agridirect/kyc",
                            "public_id", "kyc_" + userId,
                            "overwrite", true,
                            "resource_type", "auto"));
            return result.get("secure_url").toString();
        } catch (Exception e) {
            throw new ApiException("Failed to upload KYC document: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            // Extract publicId: folder/filename without extension
            String[] parts = imageUrl.split("/");
            String fileWithExt = parts[parts.length - 1];
            String fileName = fileWithExt.contains(".") ? fileWithExt.substring(0, fileWithExt.lastIndexOf('.')) : fileWithExt;
            String folder = parts[parts.length - 2];
            String publicId = folder + "/" + fileName;
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new ApiException("Failed to delete image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
