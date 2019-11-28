package hibernatebasic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data // Геттер + Сеттер
@NoArgsConstructor // Для JPA
@AllArgsConstructor
@Entity
@Table(name = "PROFESSOR")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prof_generator")
    @SequenceGenerator(name = "prof_generator", sequenceName = "prof_seq")
    private Long id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    private String lastName;

    public Professor(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
