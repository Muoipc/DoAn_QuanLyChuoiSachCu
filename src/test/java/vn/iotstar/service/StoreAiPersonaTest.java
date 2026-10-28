package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.dto.StoreAiConfigDTO;
import vn.iotstar.entity.Store;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tính năng Hồ sơ & Trí tuệ Trợ lý AI riêng biệt cho từng Shop (AI Persona)
 */
@ExtendWith(MockitoExtension.class)
class StoreAiPersonaTest {

    @Test
    @DisplayName("Mỗi shop có hồ sơ AI riêng: Tên AI, Giọng điệu (Tone), Mô tả quầy và Dặn dò")
    void testStoreAiPersonaFields() {
        Store store = new Store();
        store.setId(1L);
        store.setStoreName("Tổng Kho Sách Cũ TP.HCM");
        store.setAiAssistantName("Bé Mọt Thủ Đức");
        store.setAiTone("FRIENDLY");
        store.setAiShopDescription("Gần ĐH SPKT HCMUTE, có trà miễn phí.");
        store.setAiCustomFaq("Mua 3 cuốn tặng bookmark da.");

        assertEquals("Bé Mọt Thủ Đức", store.getAiAssistantName());
        assertEquals("FRIENDLY", store.getAiTone());
        assertEquals("Gần ĐH SPKT HCMUTE, có trà miễn phí.", store.getAiShopDescription());
        assertEquals("Mua 3 cuốn tặng bookmark da.", store.getAiCustomFaq());
    }

    @Test
    @DisplayName("Hai chi nhánh khác nhau có AI độc lập về tên và giọng điệu")
    void testIndependentAiAcrossBranches() {
        Store thuDucStore = new Store();
        thuDucStore.setId(1L);
        thuDucStore.setStoreName("Tổng Kho Thủ Đức");
        thuDucStore.setAiAssistantName("Bé Mọt Thủ Đức");
        thuDucStore.setAiTone("ENERGETIC");

        Store q1Store = new Store();
        q1Store.setId(2L);
        q1Store.setStoreName("Đường Sách Quận 1");
        q1Store.setAiAssistantName("Cậu Ba Đường Sách");
        q1Store.setAiTone("VINTAGE");

        assertNotEquals(thuDucStore.getAiAssistantName(), q1Store.getAiAssistantName());
        assertNotEquals(thuDucStore.getAiTone(), q1Store.getAiTone());
        assertEquals("Bé Mọt Thủ Đức", thuDucStore.getAiAssistantName());
        assertEquals("Cậu Ba Đường Sách", q1Store.getAiAssistantName());
    }

    @Test
    @DisplayName("Kiểm tra chuyển đổi DTO Cấu hình AI quầy")
    void testStoreAiConfigDTO() {
        StoreAiConfigDTO dto = new StoreAiConfigDTO(
                1L, "Tổng Kho", "Bé Mọt", "FRIENDLY", "Shop description", "Custom FAQ", true
        );

        assertEquals(1L, dto.getStoreId());
        assertEquals("Bé Mọt", dto.getAssistantName());
        assertEquals("FRIENDLY", dto.getTone());
        assertTrue(dto.getAiReplyEnabled());
    }
}
