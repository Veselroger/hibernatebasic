package hibernatebasic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data // Геттер + Сеттер
@NoArgsConstructor // Для JPA
@AllArgsConstructor
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;

    @ManyToOne
    private Professor professor;

    public Course(String title) {
        this.title = title;
    }

    public void setProfessor(Professor professor) {
        if (this.professor != null) {
            this.professor = null;
        }
        this.professor = professor;
        if (this.professor != null) {
            professor.getCourses().add(this);
        }
    }
}
