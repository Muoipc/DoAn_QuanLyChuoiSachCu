/**
 * ============================================================================
 * JAVASCRIPT ĐA NGÔN NGỮ (I18N): TIẾNG VIỆT & ENGLISH
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * ============================================================================
 */

const I18N_DICTIONARY = {
    vi: {
        'Ký Gửi Sách Cũ': 'Ký Gửi Sách Cũ',
        'Chuỗi 5 Cửa Hàng TP.HCM': 'Chuỗi 5 Cửa Hàng TP.HCM',
        'Trợ Lý AI Tư Vấn': 'Trợ Lý AI Tư Vấn',
        'Đơn Mua Của Tôi': 'Đơn Mua Của Tôi',
        'Đăng Nhập': 'Đăng Nhập',
        'Đăng Ký': 'Đăng Ký',
        'Giỏ Hàng': 'Giỏ Hàng',
        'Tìm Kiếm': 'Tìm Kiếm',
        'Tìm kiếm sách cũ theo tựa đề, tác giả, ISBN...': 'Tìm kiếm sách cũ theo tựa đề, tác giả, ISBN...',
        'Tìm kiếm thêm sách cũ kiểm định giá rẻ...': 'Tìm kiếm thêm sách cũ kiểm định giá rẻ...',
        'FLASH SALE SÁCH CŨ': 'FLASH SALE SÁCH CŨ',
        'GỢI Ý HÔM NAY CHO BẠN': 'GỢI Ý HÔM NAY CHO BẠN',
        'DANH MỤC SÁCH CŨ NỔI BẬT': 'DANH MỤC SÁCH CŨ NỔI BẬT',
        'Thêm Vào Giỏ Hàng': 'Thêm Vào Giỏ Hàng',
        'Mua Ngay': 'Mua Ngay',
        'Hỏi Trợ Lý AI': 'Hỏi Trợ Lý AI',
        'CHI TIẾT SẢN PHẨM': 'CHI TIẾT SẢN PHẨM',
        'MÔ TẢ SẢN PHẨM': 'MÔ TẢ SẢN PHẨM',
        'ĐÁNH GIÁ SẢN PHẨM': 'ĐÁNH GIÁ SẢN PHẨM',
        'Sản Phẩm': 'Sản Phẩm',
        'Đơn Giá': 'Đơn Giá',
        'Số Lượng': 'Số Lượng',
        'Số Tiền': 'Số Tiền',
        'Thao Tác': 'Thao Tác',
        'Xóa': 'Xóa',
        'Chọn Tất Cả': 'Chọn Tất Cả',
        'MUA HÀNG': 'MUA HÀNG',
        'Tổng thanh toán': 'Tổng thanh toán',
        'Tiết kiệm được': 'Tiết kiệm được',
        'Đã Bán': 'Đã Bán',
        'Đánh Giá': 'Đánh Giá'
    },
    en: {
        'Ký Gửi Sách Cũ': 'Book Consignments',
        'Chuỗi 5 Cửa Hàng TP.HCM': '5 HCMC Stores',
        'Trợ Lý AI Tư Vấn': 'AI Assistant',
        'Đơn Mua Của Tôi': 'My Purchases',
        'Đăng Nhập': 'Sign In',
        'Đăng Ký': 'Sign Up',
        'Giỏ Hàng': 'Cart',
        'Tìm Kiếm': 'Search',
        'Tìm kiếm sách cũ theo tựa đề, tác giả, ISBN...': 'Search used books by title, author, ISBN...',
        'Tìm kiếm thêm sách cũ kiểm định giá rẻ...': 'Search more verified used books...',
        'FLASH SALE SÁCH CŨ': 'USED BOOKS FLASH SALE',
        'GỢI Ý HÔM NAY CHO BẠN': 'DAILY DISCOVER FOR YOU',
        'DANH MỤC SÁCH CŨ NỔI BẬT': 'FEATURED CATEGORIES',
        'Thêm Vào Giỏ Hàng': 'Add To Cart',
        'Mua Ngay': 'Buy Now',
        'Hỏi Trợ Lý AI': 'Ask AI Assistant',
        'CHI TIẾT SẢN PHẨM': 'SPECIFICATIONS',
        'MÔ TẢ SẢN PHẨM': 'PRODUCT DESCRIPTION',
        'ĐÁNH GIÁ SẢN PHẨM': 'PRODUCT REVIEWS',
        'Sản Phẩm': 'Product',
        'Đơn Giá': 'Unit Price',
        'Số Lượng': 'Quantity',
        'Số Tiền': 'Total',
        'Thao Tác': 'Actions',
        'Xóa': 'Delete',
        'Chọn Tất Cả': 'Select All',
        'MUA HÀNG': 'CHECK OUT',
        'Tổng thanh toán': 'Total Payment',
        'Tiết kiệm được': 'Total Savings',
        'Đã Bán': 'Sold',
        'Đánh Giá': 'Reviews'
    }
};

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

function getCurrentLang() {
    const urlParams = new URLSearchParams(window.location.search);
    const langParam = urlParams.get('lang');
    if (langParam && (langParam === 'en' || langParam === 'vi')) {
        return langParam;
    }
    const cookieLang = getCookie('app_lang');
    if (cookieLang && (cookieLang === 'en' || cookieLang === 'vi')) {
        return cookieLang;
    }
    return 'vi';
}

function switchLanguage(lang) {
    // Lưu cookie 30 ngày
    document.cookie = `app_lang=${lang}; path=/; max-age=2592000; SameSite=Lax`;
    
    // Cập nhật URL và reload
    const url = new URL(window.location.href);
    url.searchParams.set('lang', lang);
    window.location.href = url.toString();
}

function applyClientTranslations() {
    const lang = getCurrentLang();
    
    // Cập nhật nhãn Topbar
    const langLabel = document.getElementById('currentLangLabel');
    if (langLabel) {
        langLabel.innerHTML = lang === 'en' 
            ? '<span>🇬🇧 English</span> <i class="fa-solid fa-chevron-down ms-1" style="font-size:10px;"></i>' 
            : '<span>🇻🇳 Tiếng Việt</span> <i class="fa-solid fa-chevron-down ms-1" style="font-size:10px;"></i>';
    }

    if (lang === 'en') {
        const dict = I18N_DICTIONARY.en;
        
        // Dịch placeholders
        document.querySelectorAll('input[placeholder]').forEach(input => {
            const ph = input.getAttribute('placeholder');
            if (dict[ph]) {
                input.setAttribute('placeholder', dict[ph]);
            }
        });

        // Dịch các text nodes chính
        function walkTextNodes(el) {
            if (el.nodeType === Node.TEXT_NODE) {
                const trimmed = el.nodeValue.trim();
                if (dict[trimmed]) {
                    el.nodeValue = el.nodeValue.replace(trimmed, dict[trimmed]);
                }
            } else {
                for (let child of el.childNodes) {
                    walkTextNodes(child);
                }
            }
        }
        walkTextNodes(document.body);
    }
}

document.addEventListener('DOMContentLoaded', applyClientTranslations);
