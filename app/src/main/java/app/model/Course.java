package app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data // Геттер + Сеттер
@NoArgsConstructor // Для JPA
@AllArgsConstructor
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;

    @OneToMany(mappedBy = "course")
    private List<Student> students = new ArrayList<>();

    public Course(String title) {
        this.title = title;
    }

    public void addSdudent(Student student) {
        this.students.add(student);
        student.setCourse(this);
    }
}