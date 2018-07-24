package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * created by SupriseMF
 * date:2018-07-20
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
