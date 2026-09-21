package com.student;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentServiceTest {

    @Test
    void addStudentSuccessfully() {

        StudentService service = new StudentService();

        Student student =
                new Student("SV001", "Nguyen Van An", 8.5);

        boolean result =
                service.addStudent(student);

        assertTrue(result);
    }

    @Test
    void cannotAddDuplicateStudent() {

        StudentService service = new StudentService();

        Student student1 =
                new Student("SV001", "Nguyen Van An", 8.5);

        Student student2 =
                new Student("SV001", "Tran Van B", 7.5);

        assertTrue(service.addStudent(student1));

        assertFalse(service.addStudent(student2));
    }
    @Test
    void findStudentSuccessfully() {

        StudentService service = new StudentService();

        Student student =
                new Student("SV001", "Nguyen Van An", 8.5);

        service.addStudent(student);

        Student result =
                service.findStudentById("SV001");

        assertNotNull(result);

        assertEquals("SV001", result.getId());

        assertEquals(
                "Nguyen Van An",
                result.getName()
        );

        assertEquals(
                8.5,
                result.getScore()
        );
    }
    @Test
    void updateStudentSuccessfully() {

        StudentService service = new StudentService();

        Student student =
                new Student("SV001", "Nguyen Van An", 8.5);

        service.addStudent(student);

        boolean result =
                service.updateStudent(
                        "SV001",
                        "Nguyen Van An Updated",
                        9.5
                );

        assertTrue(result);

        Student updated =
                service.findStudentById("SV001");

        assertEquals(
                "Nguyen Van An Updated",
                updated.getName()
        );

        assertEquals(
                9.5,
                updated.getScore()
        );
    }
    @Test
    void deleteStudentSuccessfully() {

        StudentService service = new StudentService();

        Student student =
                new Student("SV001", "Nguyen Van An", 8.5);

        service.addStudent(student);

        boolean result =
                service.deleteStudent("SV001");

        assertTrue(result);

        assertNull(
                service.findStudentById("SV001")
        );
    }
}
