package hibernatebasic.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@NoArgsConstructor // Для JPA
@Setter @Getter
public class Account {
    @Id
    private Long id;

    private String name;
}
