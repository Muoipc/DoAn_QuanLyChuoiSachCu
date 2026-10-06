/**
 * ============================================================================
 * JAVASCRIPT: SHOPEE CART POPOVER (XEM NHANH GIỎ HÀNG KHI HOVER)
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Chuẩn Shopee tinh gọn: bo góc 4px phẳng, sắc nét, có tam giác chỉ icon giỏ hàng
 * ============================================================================
 */
(function() {
    'use strict';

    // 1. INJECT CSS ĐỒNG BỘ CHUẨN SHOPEE TINH GỌN (SQUARE CONTROLS 4PX)
    const styleId = 'shopee-cart-popover-styles';
    if (!document.getElementById(styleId)) {
        const style = document.createElement('style');
        style.id = styleId;
        style.innerHTML = `
            .header-main-row,
            .shopee-header-section,
            .header-wrap {
                position: relative !important;
                z-index: 99999 !important;
            }

            .shopee-cart-wrap {
                position: relative !important;
                display: inline-flex !important;
                align-items: center;
                cursor: pointer;
                z-index: 100000 !important;
            }

            /* POPOVER GIỎ HÀNG CHUẨN APPLE LIQUID GLASS: ĐỒNG NHẤT MÀU & ĐỘ TRONG SUỐT, BO GÓC 18PX */
            .shopee-cart-popover {
                position: absolute !important;
                top: calc(100% + 8px) !important;
                right: 0 !important;
                width: 370px !important;
                background: rgba(255, 255, 255, 0.65) !important;
                backdrop-filter: blur(32px) saturate(200%) brightness(102%) !important;
                -webkit-backdrop-filter: blur(32px) saturate(200%) brightness(102%) !important;
                border-radius: 18px !important;
                box-shadow: 0 20px 48px -6px rgba(45, 24, 15, 0.12), inset 0 1.5px 2px 0 rgba(255, 255, 255, 0.95) !important;
                border: 1.5px solid rgba(255, 255, 255, 0.85) !important;
                z-index: 100001 !important;
                display: none;
                cursor: default;
                transform-origin: calc(100% - 20px) top;
                animation: popoverFadeIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);
                box-sizing: border-box !important;
                padding: 0 !important;
                overflow: hidden !important;
            }

            @keyframes popoverFadeIn {
                from { opacity: 0; transform: translateY(-6px) scale(0.98); }
                to { opacity: 1; transform: translateY(0) scale(1); }
            }

            /* Bỏ mũi tên trỏ giỏ hàng */
            .shopee-cart-arrow {
                display: none !important;
            }

            /* Cầu nối hover tàng hình: phủ toàn bộ khoảng cách giữa cart icon và popover */
            .shopee-cart-wrap::after {
                content: '';
                position: absolute;
                top: 100%;
                left: -120px;
                right: 0;
                height: 18px;
                display: none;
                background: transparent;
                z-index: 100002;
            }
            .shopee-cart-wrap:hover::after {
                display: block;
            }

            .shopee-cart-popover::before {
                content: '' !important;
                position: absolute !important;
                top: -16px !important;
                left: 0 !important;
                right: 0 !important;
                height: 16px !important;
                background: transparent !important;
                display: block !important;
            }

            .shopee-cart-wrap:hover .shopee-cart-popover {
                display: block !important;
            }

            .popover-header-title {
                font-size: 13px;
                font-weight: 700;
                color: #57534E;
                padding: 12px 16px;
                background: transparent !important;
                backdrop-filter: none !important;
                -webkit-backdrop-filter: none !important;
                border-bottom: 1px solid rgba(255, 255, 255, 0.65);
                border-radius: 18px 18px 0 0;
                text-transform: capitalize;
            }

            .popover-items-list {
                list-style: none;
                max-height: 290px;
                overflow-y: auto;
                margin: 0;
                padding: 0;
                background: transparent !important;
            }
            .popover-items-list::-webkit-scrollbar {
                width: 4px;
            }
            .popover-items-list::-webkit-scrollbar-thumb {
                background: rgba(232, 222, 200, 0.7);
                border-radius: 4px;
            }

            .popover-item-link {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 11px 16px;
                text-decoration: none;
                color: inherit;
                background: transparent !important;
                border-bottom: 1px solid rgba(255, 255, 255, 0.45);
                transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
                box-sizing: border-box;
            }
            .popover-item-link:hover {
                background: rgba(255, 255, 255, 0.42) !important;
                box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.5) !important;
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
                height: 50px;
                object-fit: cover;
                border: 1px solid rgba(232, 222, 200, 0.8);
                border-radius: 8px;
                flex-shrink: 0;
                background: #FAF8F5;
                box-shadow: 0 2px 6px rgba(45, 24, 15, 0.05);
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
                font-size: 11px;
                font-weight: 700;
                color: #15803d;
                background: rgba(22, 163, 74, 0.12);
                padding: 2px 7px;
                border-radius: 6px;
                display: inline-block;
                width: fit-content;
                border: 1px solid rgba(22, 163, 74, 0.15);
            }

            .popover-item-price {
                font-size: 14px;
                font-weight: 800;
                color: #ee4d2d !important;
                white-space: nowrap;
                text-align: right;
            }

            /* Chân Popover Liquid Glass */
            .popover-footer-bar {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 16px;
                background: transparent !important;
                backdrop-filter: none !important;
                -webkit-backdrop-filter: none !important;
                border-top: 1px solid rgba(255, 255, 255, 0.65);
                border-radius: 0 0 18px 18px;
            }

            .popover-more-txt {
                font-size: 12px;
                color: #78716C;
                font-weight: 600;
            }

            .popover-btn-view-cart {
                background: linear-gradient(135deg, #8C4A27 0%, #733615 100%) !important;
                color: #ffffff !important;
                font-size: 12.5px;
                font-weight: 700;
                border: none;
                border-radius: 10px !important;
                padding: 8px 18px;
                cursor: pointer;
                transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
                text-decoration: none;
                display: inline-block;
                box-shadow: 0 3px 10px rgba(140, 74, 39, 0.25), inset 0 1px 1px rgba(255, 255, 255, 0.3) !important;
            }
            .popover-btn-view-cart:hover {
                transform: translateY(-1px);
                box-shadow: 0 5px 14px rgba(140, 74, 39, 0.35) !important;
            }

            /* Khi giỏ trống */
            .popover-empty-box {
                padding: 40px 16px;
                text-align: center;
                color: #78716C;
                background: transparent;
                border-radius: 16px;
            }
            .popover-empty-icon {
                font-size: 42px;
                color: #d5cec4;
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
