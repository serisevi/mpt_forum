package com.example.forummpt.repo;

import com.example.forummpt.models.PersonalInformation;
import com.example.forummpt.models.Specializations;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PersonalInformationRepo extends JpaRepository<PersonalInformation, Long> {
    PersonalInformation searchById(long id);
    List<PersonalInformation> searchByLastnameContainsOrFirstnameContainsOrMiddlenameContainsOrDescriptionContainsOrImageUrlContains(String text1, String text2, String text3, String text4, String text5);
    List<PersonalInformation> searchByCourse(int course);
    List<PersonalInformation> searchBySpecialization(Specializations specialization);
}
