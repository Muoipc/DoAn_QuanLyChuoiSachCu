package vn.iotstar.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.iotstar.entity.User;

import java.util.Collection;
import java.util.List;

/**
 * Lớp chuyển đổi đối tượng User của cơ sở dữ liệu sang chuẩn UserDetails của Spring Security.
 * Cho phép lưu trữ thông tin đăng nhập trong SecurityContext (id, email, fullName, vai trò,...).
 */
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final String avatar;
    private final String roleName;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.avatar = user.getAvatar();
        this.roleName = user.getRole().getName();
        this.enabled = Boolean.TRUE.equals(user.getEnabled());
        // Phân quyền theo tên vai trò (ví dụ: ROLE_ADMIN, ROLE_USER, ROLE_MANAGER, ROLE_SHIPPER)
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().getName()));
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getAvatar() { return avatar; }
    public String getRoleName() { return roleName; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return enabled; }
}
