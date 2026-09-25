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
                top: calc(100% + 8px) !important;
                right: -10px !important;
                width: 410px !important;
                background: rgba(255, 255, 255, 0.70) !important;
                backdrop-filter: blur(28px) saturate(190%) brightness(102%) !important;
                -webkit-backdrop-filter: blur(28px) saturate(190%) brightness(102%) !important;
                border-radius: 20px !important;
                box-shadow: 
                    0 20px 48px -4px rgba(45, 24, 15, 0.12),
                    0 4px 14px -2px rgba(45, 24, 15, 0.04),
                    inset 0 1.5px 1.5px 0 rgba(255, 255, 255, 0.95) !important;
                border: 1.5px solid rgba(255, 255, 255, 0.88) !important;
                z-index: 100001 !important;
                display: none;
                cursor: default;
                transform-origin: calc(100% - 24px) top;
                animation: popoverFadeIn 0.22s cubic-bezier(0.16, 1, 0.3, 1);
                isolation: isolate !important;
                padding: 6px !important;
                box-sizing: border-box !important;
            }

            @keyframes popoverFadeIn {
                from { opacity: 0; transform: translateY(-6px) scale(0.97); }
                to { opacity: 1; transform: translateY(0) scale(1); }
            }

            .shopee-cart-popover::before {
                display: none !important;
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
                font-size: 13.5px;
                font-weight: 800;
                color: #1C1917;
                padding: 12px 16px;
                text-transform: capitalize;
                background: rgba(255, 255, 255, 0.40) !important;
                border: 1px solid rgba(255, 255, 255, 0.70) !important;
                border-radius: 14px;
                margin-bottom: 6px;
                letter-spacing: -0.2px;
            }

            .popover-items-list {
                list-style: none;
                max-height: 290px;
                overflow-y: auto;
                margin: 0;
                padding: 2px;
                display: flex;
                flex-direction: column;
                gap: 6px;
                background: transparent;
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
                background: rgba(255, 255, 255, 0.48) !important;
                border: 1px solid rgba(255, 255, 255, 0.75) !important;
                border-radius: 14px !important;
                transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
                box-sizing: border-box;
            }
            .popover-item-link:hover {
                background: rgba(255, 255, 255, 0.88) !important;
                transform: translateY(-2px) scale(1.01) !important;
                box-shadow: 0 8px 20px rgba(140, 74, 39, 0.12), inset 0 1px 1px rgba(255, 255, 255, 1) !important;
                border-color: rgba(255, 255, 255, 1) !important;
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
                width: 48px;
                height: 52px;
                object-fit: cover;
                border: 1px solid rgba(255, 255, 255, 0.95);
                border-radius: 10px !important;
                box-shadow: 0 2px 8px rgba(45, 24, 15, 0.08);
                flex-shrink: 0;
                background: #ffffff;
            }

            .popover-text-meta {
                display: flex;
                flex-direction: column;
                gap: 3px;
                overflow: hidden;
            }

            .popover-item-title {
                font-size: 13.5px;
                font-weight: 800;
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
                font-weight: 750;
                color: #15803d;
                background: linear-gradient(135deg, rgba(22, 163, 74, 0.12) 0%, rgba(34, 197, 94, 0.18) 100%);
                padding: 3px 8px;
                border-radius: 6px;
                display: inline-block;
                width: fit-content;
                border: 1px solid rgba(22, 163, 74, 0.25);
            }

            .popover-item-price {
                font-size: 15px;
                font-weight: 800;
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
                background: rgba(255, 255, 255, 0.40) !important;
                border: 1px solid rgba(255, 255, 255, 0.70) !important;
                border-radius: 14px;
                margin-top: 6px;
            }

            .popover-more-txt {
                font-size: 12.5px;
                color: #574E45;
                font-weight: 600;
            }

            .popover-btn-view-cart {
                background: linear-gradient(135deg, #8C4A27 0%, #6B3416 100%) !important;
                color: #ffffff !important;
                font-size: 13px;
                font-weight: 800;
                border: none;
                border-radius: 9999px !important;
                padding: 9px 22px;
                cursor: pointer;
                transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
                text-decoration: none;
                display: inline-block;
                text-transform: capitalize;
                box-shadow: 0 6px 18px rgba(140, 74, 39, 0.35);
            }
            .popover-btn-view-cart:hover {
                transform: translateY(-2px) scale(1.02);
                box-shadow: 0 8px 22px rgba(140, 74, 39, 0.45);
            }
            .popover-btn-view-cart:active {
                transform: scale(0.96);
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
