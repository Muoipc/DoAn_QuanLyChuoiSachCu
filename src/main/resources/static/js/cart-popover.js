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

            /* POPOVER GIỎ HÀNG CHUẨN SHOPEE: GỌN GÀNG, SẮC NÉT, BO GÓC 4PX */
            .shopee-cart-popover {
                position: absolute !important;
                top: calc(100% + 10px) !important;
                right: 0 !important;
                width: 360px !important;
                background: #ffffff !important;
                border-radius: 4px !important;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15) !important;
                border: 1px solid rgba(0, 0, 0, 0.09) !important;
                z-index: 100001 !important;
                display: none;
                cursor: default;
                transform-origin: calc(100% - 20px) top;
                animation: popoverFadeIn 0.18s ease-out;
                box-sizing: border-box !important;
                padding: 0 !important;
            }

            @keyframes popoverFadeIn {
                from { opacity: 0; transform: translateY(-4px); }
                to { opacity: 1; transform: translateY(0); }
            }

            /* Mũi tên tam giác trỏ thẳng lên giỏ hàng chuẩn Shopee */
            .shopee-cart-arrow {
                position: absolute !important;
                top: -6px !important;
                right: 22px !important;
                width: 12px !important;
                height: 12px !important;
                background: #ffffff !important;
                transform: rotate(45deg) !important;
                border-top: 1px solid rgba(0, 0, 0, 0.12) !important;
                border-left: 1px solid rgba(0, 0, 0, 0.12) !important;
                z-index: 100010 !important;
                display: block !important;
            }

            .shopee-cart-popover::before {
                display: none !important;
            }

            /* Cầu nối hover để rê chuột từ icon xuống menu không bị mất */
            .shopee-cart-wrap::after {
                content: '';
                position: absolute;
                top: 100%;
                left: 0;
                right: 0;
                height: 14px;
                display: none;
            }
            .shopee-cart-wrap:hover::after {
                display: block;
            }

            .shopee-cart-wrap:hover .shopee-cart-popover {
                display: block !important;
            }

            .popover-header-title {
                font-size: 13px;
                font-weight: 600;
                color: #78716C;
                padding: 10px 14px;
                background: #FAF8F5;
                border-bottom: 1px solid #F0ECE4;
                border-radius: 4px 4px 0 0;
                text-transform: capitalize;
            }

            .popover-items-list {
                list-style: none;
                max-height: 280px;
                overflow-y: auto;
                margin: 0;
                padding: 0;
                background: #ffffff;
            }
            .popover-items-list::-webkit-scrollbar {
                width: 4px;
            }
            .popover-items-list::-webkit-scrollbar-thumb {
                background: #e8dec8;
                border-radius: 2px;
            }

            .popover-item-link {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 10px 14px;
                text-decoration: none;
                color: inherit;
                background: #ffffff;
                border-bottom: 1px solid #F5F1EA;
                transition: background 0.15s ease;
                box-sizing: border-box;
            }
            .popover-item-link:hover {
                background: #FAF6F0;
            }

            .popover-item-left {
                display: flex;
                align-items: center;
                gap: 10px;
                overflow: hidden;
                flex: 1;
                padding-right: 12px;
            }

            .popover-thumb-img {
                width: 42px;
                height: 48px;
                object-fit: cover;
                border: 1px solid #E8DEC8;
                border-radius: 3px;
                flex-shrink: 0;
                background: #FAF8F5;
            }

            .popover-text-meta {
                display: flex;
                flex-direction: column;
                gap: 2px;
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
                font-weight: 600;
                color: #15803d;
                background: rgba(22, 163, 74, 0.1);
                padding: 1px 6px;
                border-radius: 3px;
                display: inline-block;
                width: fit-content;
            }

            .popover-item-price {
                font-size: 14px;
                font-weight: 700;
                color: #ee4d2d !important;
                white-space: nowrap;
                text-align: right;
            }

            /* Chân Popover */
            .popover-footer-bar {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 10px 14px;
                background: #FAF8F5;
                border-top: 1px solid #F0ECE4;
                border-radius: 0 0 4px 4px;
            }

            .popover-more-txt {
                font-size: 12px;
                color: #78716C;
                font-weight: 500;
            }

            .popover-btn-view-cart {
                background: #8C4A27;
                color: #ffffff !important;
                font-size: 12.5px;
                font-weight: 700;
                border: none;
                border-radius: 4px;
                padding: 7px 16px;
                cursor: pointer;
                transition: background 0.15s ease;
                text-decoration: none;
                display: inline-block;
            }
            .popover-btn-view-cart:hover {
                background: #733615;
            }

            /* Khi giỏ trống */
            .popover-empty-box {
                padding: 36px 16px;
                text-align: center;
                color: #78716C;
                background: #ffffff;
                border-radius: 4px;
            }
            .popover-empty-icon {
                font-size: 42px;
                color: #d5cec4;
                margin-bottom: 10px;
            }
            .popover-empty-text {
                font-size: 13px;
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

        const arrowHtml = '<div class="shopee-cart-arrow"></div>';

        if (total === 0 || items.length === 0) {
            const emptyTxt = isEn ? 'No Products Yet' : 'Chưa Có Sản Phẩm';
            return `
                ${arrowHtml}
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
            ${arrowHtml}
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
