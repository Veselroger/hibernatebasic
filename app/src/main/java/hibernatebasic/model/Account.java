package hibernatebasic.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data // Добавит геттер + сеттер
@NoArgsConstructor // Для JPA
@Entity
public class Account {
    @Id
    private Long id;

    private String name;
}
