package app.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
@Entity
@Table(name = "STUDENTS")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stud_gen")
    @SequenceGenerator(name = "stud_gen", sequenceName = "stud_seq")
    private Long id;

    private String name;

    @OneToOne
    private Account account;

    @ManyToOne
    private Course course;

    public void setAccount(Account acc) {
        this.account = acc;
        acc.setStudent(this);
    }
}
