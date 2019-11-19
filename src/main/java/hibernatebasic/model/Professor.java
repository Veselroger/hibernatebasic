package hibernatebasic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data // Геттер + Сеттер
@NoArgsConstructor // Для JPA
@AllArgsConstructor // Для удобства
public class Professor {
    @Id
    private Long id;

    private String firstName;
    private String lastName;
}
