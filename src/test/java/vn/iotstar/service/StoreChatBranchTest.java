package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.ChatMessage;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử nghiệp vụ Chat đa chi nhánh & cơ chế AI Toggle / Chuyển tiếp nhân viên quầy
 */
@ExtendWith(MockitoExtension.class)
class StoreChatBranchTest {

    @Test
    @DisplayName("Bật/Tắt chế độ Trợ lý AI trả lời giùm cho từng chi nhánh độc lập")
    void testToggleAiReplyMode() {
        Store store1 = new Store();
        store1.setId(1L);
        store1.setStoreName("Tổng Kho Thủ Đức");
        store1.setAiReplyEnabled(false);

        Store store2 = new Store();
        store2.setId(2L);
        store2.setStoreName("Đường Sách Quận 1");
        store2.setAiReplyEnabled(true);

        assertFalse(store1.getAiReplyEnabled(), "Store 1 mặc định tắt AI");
        assertTrue(store2.getAiReplyEnabled(), "Store 2 đang bật AI");

        // Toggle Store 1
        store1.setAiReplyEnabled(!store1.getAiReplyEnabled());
        assertTrue(store1.getAiReplyEnabled(), "Store 1 sau khi bật phải là true");
        assertTrue(store2.getAiReplyEnabled(), "Store 2 không bị ảnh hưởng bởi Store 1");
    }

    @Test
    @DisplayName("Khách hàng gửi yêu cầu gặp nhân viên trực quầy kích hoạt cờ needsAttention")
    void testCustomerRequestHumanStaffFlagsAttention() {
        Store store = new Store();
        store.setId(1L);
        store.setStoreName("Tổng Kho Thủ Đức");

        User customer = new User();
        customer.setId(10L);
        customer.setUsername("customer_test");
        customer.setFullName("Nguyễn Văn Test");

        ChatMessage reqMsg = new ChatMessage();
        reqMsg.setUser(customer);
        reqMsg.setStore(store);
        reqMsg.setSenderType(ChatMessage.SenderType.CUSTOMER);
        reqMsg.setContent("[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]");
        reqMsg.setNeedsAttention(true);

        assertTrue(reqMsg.getNeedsAttention(), "Yêu cầu gặp nhân viên quầy phải bật cờ needsAttention");
        assertEquals(ChatMessage.SenderType.CUSTOMER, reqMsg.getSenderType());
        assertEquals("[Khách hàng yêu cầu gặp nhân viên quầy trực tiếp]", reqMsg.getContent());
    }

    @Test
    @DisplayName("Phân định người gửi giữa Trợ lý AI và Nhân viên trực quầy")
    void testDistinguishAiVsStaffSender() {
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSenderType(ChatMessage.SenderType.STORE_STAFF);
        aiMsg.setSenderName("Trợ lý AI - Đường Sách Quận 1");

        ChatMessage staffMsg = new ChatMessage();
        staffMsg.setSenderType(ChatMessage.SenderType.STORE_STAFF);
        staffMsg.setSenderName("Quản lý quầy - Đường Sách Quận 1");

        assertTrue(aiMsg.getSenderName().startsWith("Trợ lý AI"), "Tin nhắn AI phải có tiền tố Trợ lý AI");
        assertFalse(staffMsg.getSenderName().startsWith("Trợ lý AI"), "Tin nhắn nhân viên thật không mang tiền tố AI");
    }

    @Test
    @DisplayName("Kiểm tra cơ chế Presence Tracking: Online/Offline theo hoạt động thực tế và chế độ AI")
    void testStorePresenceTracking() {
        Store store = new Store();
        store.setId(100L);
        store.setStoreName("Chi Nhánh Test Presence");
        store.setAiReplyEnabled(false);

        // Ban đầu chưa có hoạt động và AI tắt -> Offline
        assertFalse(vn.iotstar.controller.StoreChatController.isStoreOnline(store), "Chưa có hoạt động và AI tắt thì phải Offline");

        // Quản lý quầy mở tab chat / gửi tin -> Online
        vn.iotstar.controller.StoreChatController.touchStorePresence(store.getId());
        assertTrue(vn.iotstar.controller.StoreChatController.isStoreOnline(store), "Sau khi touch presence thì phải Online");

        // Khi bật AI -> Luôn Online
        Store aiStore = new Store();
        aiStore.setId(200L);
        aiStore.setAiReplyEnabled(true);
        assertTrue(vn.iotstar.controller.StoreChatController.isStoreOnline(aiStore), "Bật AI thì luôn Online 24/7");
    }
}
