package vn.iotstar.dto;

import vn.iotstar.entity.StoreRegistration;

public class SellerApplyFormDTO {
    // Thông tin cửa hàng
    private String storeName;
    private String phone;
    private String email;
    private String address;
    private String province = "TP. Hồ Chí Minh";
    private String district;
    private String openTime = "08:00";
    private String closeTime = "21:30";
    private String description;
    private String image;

    // Định danh chủ quầy & ngân hàng
    private String ownerName;
    private String ownerIdCard;
    private String bankName;
    private String bankAccount;

    // Dành cho khách vãng lai chưa có tài khoản
    private String newUsername;
    private String newPassword;
    private String confirmPassword;

    public SellerApplyFormDTO() {}

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerIdCard() { return ownerIdCard; }
    public void setOwnerIdCard(String ownerIdCard) { this.ownerIdCard = ownerIdCard; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getNewUsername() { return newUsername; }
    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public StoreRegistration toEntity() {
        StoreRegistration reg = new StoreRegistration();
        reg.setStoreName(this.storeName);
        reg.setPhone(this.phone);
        reg.setEmail(this.email);
        reg.setAddress(this.address);
        reg.setProvince(this.province != null && !this.province.isBlank() ? this.province : "TP. Hồ Chí Minh");
        reg.setDistrict(this.district);
        reg.setOpenTime(this.openTime != null && !this.openTime.isBlank() ? this.openTime : "08:00");
        reg.setCloseTime(this.closeTime != null && !this.closeTime.isBlank() ? this.closeTime : "21:30");
        reg.setDescription(this.description);
        reg.setImage(this.image);
        reg.setOwnerName(this.ownerName);
        reg.setOwnerIdCard(this.ownerIdCard);
        reg.setBankName(this.bankName);
        reg.setBankAccount(this.bankAccount);
        return reg;
    }
}
