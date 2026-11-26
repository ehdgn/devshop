package org.example.shopping._core.utils;

import org.example.shopping._core.errors.exception.Exception400;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    @TempDir
    Path dir;

    private MockMultipartFile file(String name, String contentType) {
        return new MockMultipartFile("image", name, contentType, new byte[]{1, 2, 3});
    }

    @Test
    void 경로를_조작한_파일명이어도_업로드_폴더_안에_UUID_이름으로_저장된다() throws Exception {
        new FileUtil().setImagesDir(dir.toString());

        String saved = FileUtil.saveFile(file("../../evil.png", "image/png"));

        assertTrue(saved.matches("[0-9a-f-]{36}\\.png"), saved);
        assertTrue(Files.exists(dir.resolve(saved)));
        try (var files = Files.list(dir)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void 이미지가_아닌_확장자는_Content_Type_이_image_여도_거부한다() {
        new FileUtil().setImagesDir(dir.toString());

        assertFalse(FileUtil.isImageFile(file("x.html", "image/png")));
        assertThrows(Exception400.class, () -> FileUtil.saveFile(file("x.html", "image/png")));
        assertThrows(Exception400.class, () -> FileUtil.saveFile(file("x.png", "text/html")));
    }

    @Test
    void 업로드_폴더_밖의_파일은_삭제하지_않는다() {
        new FileUtil().setImagesDir(dir.toString());

        assertThrows(Exception400.class, () -> FileUtil.deleteFile("../outside.png"));
    }
}
