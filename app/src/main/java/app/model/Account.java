package app.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data // Добавит геттер + сеттер
@NoArgsConstructor // Для JPA
@Entity
@Table(name = "ACCOUNT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_gen")
    @SequenceGenerator(name = "acc_gen", sequenceName = "acc_seq")
    private Long id;

    @Column(name = "ACC_NAME")
    private String name;

    @OneToOne(mappedBy = "account", cascade = CascadeType.PERSIST)
    private Student student;
}
