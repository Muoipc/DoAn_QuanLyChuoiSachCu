/**
 * ============================================================================
 * SHOPEE CART HOVER POPOVER (HIỆN BẢNG GIỎ HÀNG KHI RÊ CHUỘT)
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Đã khắc phục triệt để lỗi đè hình (Z-Index Stacking Context)
 * ============================================================================
 */

(function() {
    // 1. INJECT CSS CHO POPOVER & ĐẢM BẢO KHÔNG BỊ ĐÈ BỞI BẤT KỲ PHẦN TỬ NÀO
    const styleId = 'shopee-cart-popover-styles';
    if (!document.getElementById(styleId)) {
        const style = document.createElement('style');
        style.id = styleId;
        style.innerHTML = `
            /* Đảm bảo Header luôn ở lớp trên cùng của trang web */
            header, .header-wrap, .shopee-header-section, .cart-header-main, .search-header-main {
                position: sticky !important;
                top: 0 !important;
                z-index: 99999 !important;
            }

            .shopee-cart-wrap {
                position: relative !important;
                display: inline-flex !important;
                align-items: center;
                cursor: pointer;
                z-index: 100000 !important;
            }

            .shopee-cart-popover {
                position: absolute !important;
                top: calc(100% + 10px) !important;
                right: -10px !important;
                width: 410px !important;
                background: #ffffff !important;
                border-radius: 3px !important;
                box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.22) !important;
                border: 1px solid rgba(0, 0, 0, 0.09) !important;
                z-index: 100001 !important;
                display: none;
                cursor: default;
                transform-origin: calc(100% - 24px) top;
                animation: popoverFadeIn 0.18s cubic-bezier(0.165, 0.84, 0.44, 1);
                isolation: isolate !important; /* Tạo stacking context độc lập, chống đè hình */
            }

            @keyframes popoverFadeIn {
                from { opacity: 0; transform: scale(0.92); }
                to { opacity: 1; transform: scale(1); }
            }

            /* Mũi tên tam giác chỉ lên icon giỏ hàng */
            .shopee-cart-popover::before {
                content: '';
                position: absolute;
                top: -9px;
                right: 22px;
                border-width: 0 9px 9px 9px;
                border-style: solid;
                border-color: transparent transparent #ffffff transparent;
                filter: drop-shadow(0 -2px 1.5px rgba(0,0,0,0.06));
                z-index: 100002;
            }

            /* Vùng cầu nối hover để chuột rê từ icon xuống popover không bị mất */
            .shopee-cart-wrap::after {
                content: '';
                position: absolute;
                top: 100%;
                left: -20px;
                right: -20px;
                height: 16px;
                display: none;
            }
            .shopee-cart-wrap:hover::after {
                display: block;
            }

            /* Hover hiển thị popover */
            .shopee-cart-wrap:hover .shopee-cart-popover {
                display: block !important;
            }

            .popover-header-title {
                font-size: 13px;
                font-weight: 600;
                color: #888888;
                padding: 12px 14px 10px;
                text-transform: capitalize;
                border-bottom: 1px solid #f6f3ee;
                background: #ffffff;
            }

            .popover-items-list {
                list-style: none;
                max-height: 290px;
                overflow-y: auto;
                margin: 0;
                padding: 0;
                background: #ffffff;
            }
            .popover-items-list::-webkit-scrollbar {
                width: 5px;
            }
            .popover-items-list::-webkit-scrollbar-thumb {
                background: #e8dec8;
                border-radius: 4px;
            }

            .popover-item-link {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 10px 14px;
                text-decoration: none;
                color: inherit;
                transition: background 0.15s;
                border-bottom: 1px solid #faf6f0;
                background: #ffffff !important;
            }
            .popover-item-link:last-child {
                border-bottom: none;
            }
            .popover-item-link:hover {
                background: #FAF8F5 !important;
            }

            .popover-item-left {
                display: flex;
                align-items: center;
                gap: 12px;
                overflow: hidden;
                flex: 1;
                padding-right: 12px;
            }

            .popover-thumb-img {
                width: 44px;
                height: 48px;
                object-fit: cover;
                border: 1px solid #e8dec8;
                border-radius: 2px;
                flex-shrink: 0;
                background: #fdfbf7;
            }

            .popover-text-meta {
                display: flex;
                flex-direction: column;
                gap: 3px;
                overflow: hidden;
            }

            .popover-item-title {
                font-size: 13px;
                font-weight: 700;
                color: #1C1917;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                line-height: 1.35;
            }
            .popover-item-title:hover {
                color: #8C4A27;
            }

            .popover-item-cond {
                font-size: 11.5px;
                color: #78716C;
            }

            .popover-item-price {
                font-size: 14px;
                font-weight: 800;
                color: #8C4A27;
                white-space: nowrap;
                text-align: right;
            }

            /* Chân Popover */
            .popover-footer-bar {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 14px;
                background: #ffffff;
                border-top: 1px solid #f2ede6;
                border-radius: 0 0 3px 3px;
            }

            .popover-more-txt {
                font-size: 12.5px;
                color: #574E45;
                font-weight: 500;
            }

            .popover-btn-view-cart {
                background: #8C4A27;
                color: #ffffff !important;
                font-size: 13px;
                font-weight: 700;
                border: none;
                border-radius: 2px;
                padding: 9px 18px;
                cursor: pointer;
                transition: all 0.15s;
                text-decoration: none;
                display: inline-block;
                text-transform: capitalize;
            }
            .popover-btn-view-cart:hover {
                background: #733615;
                box-shadow: 0 2px 8px rgba(140, 74, 39, 0.3);
            }

            /* Khi giỏ trống */
            .popover-empty-box {
                padding: 42px 20px;
                text-align: center;
                color: #78716C;
                background: #ffffff;
            }
            .popover-empty-icon {
                font-size: 52px;
                color: #dcd6cd;
                margin-bottom: 12px;
            }
            .popover-empty-text {
                font-size: 13.5px;
                font-weight: 600;
                color: #574E45;
            }
        `;
        document.head.appendChild(style);
    }

    // 2. HELPER FORMAT TIỀN TỆ VNĐ
    function formatCurrency(num) {
        if (!num) return '₫0';
        return '₫' + Number(num).toLocaleString('vi-VN');
    }

    // 3. RENDER POPOVER HTML
    function buildPopoverHtml(data, isEn) {
        const total = data.totalCount || 0;
        const moreCount = data.moreCount || 0;
        const items = data.items || [];

        if (total === 0 || items.length === 0) {
            const emptyTxt = isEn ? 'No Products Yet' : 'Chưa Có Sản Phẩm';
            return `
                <div class="popover-empty-box">
                    <i class="fa-solid fa-cart-shopping popover-empty-icon"></i>
                    <div class="popover-empty-text">${emptyTxt}</div>
                </div>
            `;
        }

        const titleTxt = isEn ? 'Recently Added Products' : 'Sản Phẩm Mới Thêm';
        const viewCartTxt = isEn ? 'View My Shopping Cart' : 'Xem Giỏ Hàng';
        const moreTxt = isEn 
            ? `${moreCount > 0 ? moreCount + ' More Products In Cart' : total + ' Products In Cart'}`
            : `${moreCount > 0 ? moreCount + ' Thêm Hàng Vào Giỏ' : total + ' Sản phẩm trong giỏ'}`;

        let itemsHtml = '';
        items.forEach(it => {
            const img = it.imageUrl || '/images/books/book_1.jpg';
            const condTxt = isEn ? `${it.conditionPercent}% New (Verified)` : `${it.conditionPercent}% Mới (Kiểm Định)`;
            itemsHtml += `
                <a href="/books/${it.bookId}" class="popover-item-link">
                    <div class="popover-item-left">
                        <img src="${img}" alt="${it.title}" class="popover-thumb-img">
                        <div class="popover-text-meta">
                            <span class="popover-item-title" title="${it.title}">${it.title}</span>
                            <span class="popover-item-cond">${condTxt}</span>
                        </div>
                    </div>
                    <div class="popover-item-price">${formatCurrency(it.price)}</div>
                </a>
            `;
        });

        return `
            <div class="popover-header-title">${titleTxt}</div>
            <div class="popover-items-list">${itemsHtml}</div>
            <div class="popover-footer-bar">
                <span class="popover-more-txt">${moreTxt}</span>
                <a href="/cart" class="popover-btn-view-cart">${viewCartTxt}</a>
            </div>
        `;
    }

    // 4. CẬP NHẬT POPOVER DỮ LIỆU
    let cachedCartData = null;

    function refreshCartPopover(container) {
        const popover = container.querySelector('.shopee-cart-popover');
        if (!popover) return;

        const isEn = (typeof getCurrentLang === 'function' && getCurrentLang() === 'en') ||
                     (document.cookie && document.cookie.includes('app_lang=en'));

        fetch('/cart/api/preview')
            .then(res => res.json())
            .then(data => {
                cachedCartData = data;
                popover.innerHTML = buildPopoverHtml(data, isEn);

                // Cập nhật mọi badge số lượng
                const total = data.totalCount || 0;
                document.querySelectorAll('.shopee-cart-badge, #headerCartBadge, #cartCountBadge').forEach(badge => {
                    badge.textContent = total;
                    badge.style.display = total > 0 ? 'inline-block' : 'none';
                });
            })
            .catch(err => {
                console.warn('Lỗi nạp giỏ hàng popover:', err);
            });
    }

    // 5. KHỞI TẠO TẤT CẢ KHUNG GIỎ HÀNG TRÊN TRANG
    function initShopeeCartHover() {
        const cartWrappers = document.querySelectorAll('.shopee-cart-wrap');

        cartWrappers.forEach(wrap => {
            // Nâng cấp z-index của header cha lên mức cao nhất
            let parentHeader = wrap.closest('header, .header-wrap, .shopee-header-section, .cart-header-main, .search-header-main');
            if (parentHeader) {
                parentHeader.style.setProperty('position', 'sticky', 'important');
                parentHeader.style.setProperty('top', '0', 'important');
                parentHeader.style.setProperty('z-index', '99999', 'important');
            }

            // Nếu chưa có popover bên trong, chèn vào
            let popover = wrap.querySelector('.shopee-cart-popover');
            if (!popover) {
                popover = document.createElement('div');
                popover.className = 'shopee-cart-popover';
                popover.innerHTML = `
                    <div class="popover-empty-box" style="padding:24px;">
                        <i class="fa-solid fa-spinner fa-spin" style="font-size:24px; color:#8C4A27;"></i>
                    </div>
                `;
                wrap.appendChild(popover);
            }

            // Sự kiện khi chuột rê vào icon giỏ hàng
            wrap.addEventListener('mouseenter', function() {
                refreshCartPopover(wrap);
            });
        });

        // Tải trước số lượng và dữ liệu ban đầu
        if (cartWrappers.length > 0) {
            refreshCartPopover(cartWrappers[0]);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initShopeeCartHover);
    } else {
        initShopeeCartHover();
    }

    window.refreshShopeeCartPopover = function() {
        const wrap = document.querySelector('.shopee-cart-wrap');
        if (wrap) refreshCartPopover(wrap);
    };
})();
