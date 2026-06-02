package com.artgallery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ArtGalleryApplicationTests {

    @Test
    void contextLoads() {
        // Sanity test to ensure context starts up successfully
    }
}
