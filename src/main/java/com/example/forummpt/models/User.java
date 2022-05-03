package com.example.forummpt.models;

import com.example.forummpt.dto.UserDTO;
import com.example.forummpt.dto.UserInfoDTO;
import com.example.forummpt.repo.PersonalInformationRepo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Null;
import java.sql.Date;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "user", schema = "spring")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String username;
    private String password;
    private String email;
    private Date datetime;
    private boolean active;
    @OneToOne(optional = true)
    private PersonalInformation userInfo;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Notifications> notifications;
    @JsonIgnore
    @OneToMany(mappedBy = "bannedUser", cascade = CascadeType.ALL)
    private Set<LocalBanList> localBans;
    @JsonIgnore
    @OneToMany(mappedBy = "messageAuthor", cascade = CascadeType.ALL)
    private Set<Messages> users;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", schema = "spring",joinColumns = @JoinColumn(name="user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    public User() {
    }

    public User(UserDTO dto, PersonalInformationRepo personalInformationRepo) {
        this.id = dto.getId();
        this.username = dto.getUsername();
        this.password = dto.getPassword();
        this.email = dto.getEmail();
        this.datetime = dto.getDatetime();
        this.userInfo = personalInformationRepo.searchById(dto.getUserInfoId());
    }

    public User(long id, String username, String password, String email, Date datetime, boolean active, PersonalInformation userInfo, Set<Notifications> notifications, Set<LocalBanList> localBans, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.datetime = datetime;
        this.active = active;
        this.userInfo = userInfo;
        this.notifications = notifications;
        this.localBans = localBans;
        this.roles = roles;
    }

    public Set<LocalBanList> getLocalBans() {
        return localBans;
    }

    public void setLocalBans(Set<LocalBanList> localBans) {
        this.localBans = localBans;
    }

    public Set<Notifications> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notifications> notifications) {
        this.notifications = notifications;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public PersonalInformation getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(PersonalInformation userInfo) {
        this.userInfo = userInfo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
