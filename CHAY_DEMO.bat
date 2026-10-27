@echo off
chcp 65001 >nul
title HỆ THỐNG REBOOK - CHUỖI CỬA HÀNG SÁCH CŨ
color 0A

echo ===================================================================
echo       ĐỒ ÁN CUỐI KỲ: QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ (REBOOK)
echo                 GVHD: ThS. NGUYỄN HỮU TRUNG
echo ===================================================================
echo.

:: 1. Giải phóng cổng 8080 nếu đang bị chiếm dụng
echo [1/4] Kiểm tra và dọn dẹp cổng 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /f /pid %%a >nul 2>&1
)
echo   -^> Cổng 8080 đã sẵn sàng!
echo.

:: 2. Kiểm tra dịch vụ MySQL
echo [2/4] Kiểm tra dịch vụ CSDL MySQL80...
net start MySQL80 >nul 2>&1
echo   -^> Dịch vụ MySQL80 đang hoạt động.
echo.

:: 3. Thiết lập môi trường Java 21
echo [3/4] Thiết lập môi trường Java 21...
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "DB_PASS=cuong2006@"
set "PATH=C:\Program Files\Java\jdk-21\bin;%PATH%"
echo.

:: 4. Chạy Server Spring Boot
echo [4/4] Đang khởi động Server Spring Boot...
echo       Trình duyệt web sẽ tự động mở sau vài giây!
echo       (Vui lòng không tắt cửa sổ màu đen này khi đang xem web)
echo.
echo ===================================================================
echo  TÀI KHOẢN ĐĂNG NHẬP SẴN:
echo   - ADMIN:   admin@rebook.vn          / Pass: Admin@123
echo   - MANAGER: manager.thuduc@rebook.vn / Pass: Manager@123
echo   - SHIPPER: shipper@rebook.vn        / Pass: Shipper@123
echo   - USER:    user@rebook.vn           / Pass: User@123
echo ===================================================================
echo.

:: Tự động mở trình duyệt sau 7 giây
start "" powershell -NoProfile -Command "Start-Sleep -Seconds 7; Start-Process 'http://localhost:8080'"

:: Chạy file jar
cd /d "d:\Study\anti\DoAn_QuanLyChuoiSachCu"
"C:\Program Files\Java\jdk-21\bin\java.exe" -jar target\DoAn_QuanLyChuoiSachCu-1.0.0.jar

pause
