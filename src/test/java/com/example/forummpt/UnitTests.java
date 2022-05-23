package com.example.forummpt;

import com.example.forummpt.controllers.ApiController;
import com.example.forummpt.models.*;
import com.example.forummpt.repo.*;
import com.example.forummpt.services.PasswordGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;


@RunWith(SpringRunner.class)
@SpringBootTest
public class UnitTests {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ApiController apiController;
    @Autowired private UsersRepo usersRepo;
    @Autowired private SpecializationsRepo specializationsRepo;
    @Autowired private PersonalInformationRepo personalInformationRepo;
    @Autowired private ApiSessionsRepo apiSessionsRepo;
    @Autowired private GlobalBanListRepo globalBanListRepo;

    @Test
    public void tokenValidationTest_True() {
        // Prepare data
        Specializations specialization = new Specializations();
        specialization.setSpecialization("aezakmi");
        specializationsRepo.save(specialization);
        PersonalInformation userInfo = new PersonalInformation();
        userInfo.setFirstname("Иван");
        userInfo.setMiddlename("Иванович");
        userInfo.setLastname("Иванов");
        userInfo.setDescription("Примерный человек");
        userInfo.setSpecialization(specialization);
        userInfo.setCourse(4);
        userInfo.setImageUrl("/uploaded-images/basicavatar.jpg");
        personalInformationRepo.save(userInfo);
        User user = new User();
        user.setUsername("Zab1yaka1338");
        user.setPassword(passwordEncoder.encode("Pa$$w0rd"));
        user.setEmail("abcoc@mpt.ru");
        user.setActive(true);
        user.setDatetime(Date.valueOf(LocalDate.now()));
        user.setRoles(Collections.singleton(Role.USER));
        user.setUserInfo(userInfo);
        usersRepo.save(user);
        ApiSessions apiSessions = new ApiSessions();
        apiSessions.setUser(user);
        String token = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useUpper(true).useLower(true).usePunctuation(true).build().generate(50);
        apiSessions.setToken(token);
        apiSessionsRepo.save(apiSessions);
        // Compare expected and actual results
        Boolean state = apiController.validateToken(token);
        Assert.assertTrue(state);
        // Delete data
        apiSessionsRepo.delete(apiSessions);
        usersRepo.delete(user);
        personalInformationRepo.delete(userInfo);
        specializationsRepo.delete(specialization);
    }

    @Test
    public void tokenValidationTest_False_NoToken() {
        String token = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useUpper(true).useLower(true).usePunctuation(true).build().generate(50);
        Boolean state = apiController.validateToken(token);
        Assert.assertFalse(state);
    }

    @Test
    public void tokenValidationTest_False_NotActivatedAccount() {
        // Prepare data
        Specializations specialization = new Specializations();
        specialization.setSpecialization("aezakmi");
        specializationsRepo.save(specialization);
        PersonalInformation userInfo = new PersonalInformation();
        userInfo.setFirstname("Иван");
        userInfo.setMiddlename("Иванович");
        userInfo.setLastname("Иванов");
        userInfo.setDescription("Примерный человек");
        userInfo.setSpecialization(specialization);
        userInfo.setCourse(4);
        userInfo.setImageUrl("/uploaded-images/basicavatar.jpg");
        personalInformationRepo.save(userInfo);
        User user = new User();
        user.setUsername("Zab1yaka1338");
        user.setPassword(passwordEncoder.encode("Pa$$w0rd"));
        user.setEmail("abcoc@mpt.ru");
        user.setActive(false);
        user.setDatetime(Date.valueOf(LocalDate.now()));
        user.setRoles(Collections.singleton(Role.USER));
        user.setUserInfo(userInfo);
        usersRepo.save(user);
        ApiSessions apiSessions = new ApiSessions();
        apiSessions.setUser(user);
        String token = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useUpper(true).useLower(true).usePunctuation(true).build().generate(50);
        apiSessions.setToken(token);
        apiSessionsRepo.save(apiSessions);
        // Compare expected and actual results
        Boolean state = apiController.validateToken(token);
        Assert.assertFalse(state);
        // Delete data
        apiSessionsRepo.delete(apiSessions);
        usersRepo.delete(user);
        personalInformationRepo.delete(userInfo);
        specializationsRepo.delete(specialization);
    }

    @Test
    public void tokenValidationTest_False_ActualGlobalBan() {
        // Prepare data
        Specializations specialization = new Specializations();
        specialization.setSpecialization("aezakmi");
        specializationsRepo.save(specialization);
        PersonalInformation userInfo = new PersonalInformation();
        userInfo.setFirstname("Иван");
        userInfo.setMiddlename("Иванович");
        userInfo.setLastname("Иванов");
        userInfo.setDescription("Примерный человек");
        userInfo.setSpecialization(specialization);
        userInfo.setCourse(4);
        userInfo.setImageUrl("/uploaded-images/basicavatar.jpg");
        personalInformationRepo.save(userInfo);
        User user = new User();
        user.setUsername("Zab1yaka1338");
        user.setPassword(passwordEncoder.encode("Pa$$w0rd"));
        user.setEmail("abcoc@mpt.ru");
        user.setActive(true);
        user.setDatetime(Date.valueOf(LocalDate.now()));
        user.setRoles(Collections.singleton(Role.USER));
        user.setUserInfo(userInfo);
        usersRepo.save(user);
        GlobalBanList globalBan = new GlobalBanList();
        globalBan.setLoginBan(true);
        globalBan.setWriteBan(true);
        globalBan.setBannedUser(user);
        globalBan.setBanStartDate(Date.valueOf(LocalDate.now()));
        globalBan.setBanExpireDate(Date.valueOf(LocalDate.now().plusDays(1)));
        globalBanListRepo.save(globalBan);
        ApiSessions apiSessions = new ApiSessions();
        apiSessions.setUser(user);
        String token = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useUpper(true).useLower(true).usePunctuation(true).build().generate(50);
        apiSessions.setToken(token);
        apiSessionsRepo.save(apiSessions);
        // Compare expected and actual results
        Boolean state = apiController.validateToken(token);
        Assert.assertFalse(state);
        // Delete data
        apiSessionsRepo.delete(apiSessions);
        globalBanListRepo.delete(globalBan);
        usersRepo.delete(user);
        personalInformationRepo.delete(userInfo);
        specializationsRepo.delete(specialization);
    }

    @Test
    public void tokenValidationTest_True_ExpiredGlobalBan() {
        // Prepare data
        Specializations specialization = new Specializations();
        specialization.setSpecialization("aezakmi");
        specializationsRepo.save(specialization);
        PersonalInformation userInfo = new PersonalInformation();
        userInfo.setFirstname("Иван");
        userInfo.setMiddlename("Иванович");
        userInfo.setLastname("Иванов");
        userInfo.setDescription("Примерный человек");
        userInfo.setSpecialization(specialization);
        userInfo.setCourse(4);
        userInfo.setImageUrl("/uploaded-images/basicavatar.jpg");
        personalInformationRepo.save(userInfo);
        User user = new User();
        user.setUsername("Zab1yaka1338");
        user.setPassword(passwordEncoder.encode("Pa$$w0rd"));
        user.setEmail("abcoc@mpt.ru");
        user.setActive(true);
        user.setDatetime(Date.valueOf(LocalDate.now()));
        user.setRoles(Collections.singleton(Role.USER));
        user.setUserInfo(userInfo);
        usersRepo.save(user);
        GlobalBanList globalBan = new GlobalBanList();
        globalBan.setLoginBan(true);
        globalBan.setWriteBan(true);
        globalBan.setBannedUser(user);
        globalBan.setBanStartDate(Date.valueOf(LocalDate.now().minusDays(2)));
        globalBan.setBanExpireDate(Date.valueOf(LocalDate.now().minusDays(1)));
        globalBanListRepo.save(globalBan);
        ApiSessions apiSessions = new ApiSessions();
        apiSessions.setUser(user);
        String token = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useUpper(true).useLower(true).usePunctuation(true).build().generate(50);
        apiSessions.setToken(token);
        apiSessionsRepo.save(apiSessions);
        // Compare expected and actual results
        Boolean state = apiController.validateToken(token);
        Assert.assertTrue(state);
        // Delete data
        apiSessionsRepo.delete(apiSessions);
        globalBanListRepo.delete(globalBan);
        usersRepo.delete(user);
        personalInformationRepo.delete(userInfo);
        specializationsRepo.delete(specialization);
    }

}
