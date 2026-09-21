package com.student;

import java.util.ArrayList;
import java.util.List;

public class StudentService {

    private final List<Student> students = new ArrayList<>();

    public boolean addStudent(Student student) {

        if (student == null) {
            return false;
        }

        if (student.getId() == null ||
                student.getId().isBlank()) {
            return false;
        }

        if (student.getName() == null ||
                student.getName().isBlank()) {
            return false;
        }

        if (findStudentById(student.getId()) != null) {
            return false;
        }

        students.add(student);

        return true;
    }

    public Student findStudentById(String id) {

        for (Student student : students) {

            if (student.getId().equals(id)) {
                return student;
            }
        }

        return null;
    }

    public boolean deleteStudent(String id) {

        Student student = findStudentById(id);

        if (student == null) {
            return false;
        }

        students.remove(student);

        return true;
    }

    public boolean updateStudent(
            String id,
            String name,
            double score) {

        Student student = findStudentById(id);

        if (student == null) {
            return false;
        }

        student.setName(name);
        student.setScore(score);

        return true;
    }

    public List<Student> getAllStudents() {
        return students;
    }
}